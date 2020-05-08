package jarshrink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Deconimus
 * @author stechio (https://github.com/stechio):
 *         <ul>
 *         <li>jdeps output parsing refactored ({@link JDepsClassOutputParser} hierarchy introduced
 *         along with {@link JDepsClassOutputParser.JDeps9Parser support to jdeps 9 verbose:class
 *         output format})</li>
 *         <li>jdeps execution on Unix shell fixed</li>
 *         </ul>
 */
public class Dependencies {
    /**
     * jdeps verbose:class output parser.
     * 
     * @author stechio (https://github.com/stechio)
     */
    private static abstract class JDepsClassOutputParser implements AutoCloseable {
        public interface IListener {
            void onClassDependency(JDepsClassOutputParser source, String dependencyName);

            void onClassEnd(JDepsClassOutputParser source, String className);

            void onClassStart(JDepsClassOutputParser source, String className);
        }

        /**
         * JDK 8 jdeps verbose:class output parser.
         * 
         * @author stechio (https://github.com/stechio)
         */
        private static class JDeps8Parser extends JDepsClassOutputParser {
            private static final String Marker_Class = " (";
            private static final String Marker_ClassDependency = " -> ";

            public JDeps8Parser(IListener listener) {
                super(listener);
            }

            @Override
            public void parseLine(String line) {
                // For example:
                // "   com.google.common.io.BaseEncoding (guava-28.0-jre.jar)"
                // "      -> com.google.common.io.ByteSink                              guava-28.0-jre.jar"
                // "      -> java.lang.Class"
                int index = line.indexOf(Marker_ClassDependency);
                if (index > 0) {
                    index += Marker_ClassDependency.length();
                    int endIndex = line.indexOf(" ", index);
                    onClassDependency(
                            endIndex > 0 ? line.substring(index, endIndex) : line.substring(index));
                } else {
                    index = line.indexOf(Marker_Class);
                    if (index > 0) {
                        onClass(line.substring(0, index).trim());
                    }
                }
            }
        }

        /**
         * JDK 9+ jdeps verbose:class output parser.
         * 
         * @author stechio (https://github.com/stechio)
         */
        private static class JDeps9Parser extends JDepsClassOutputParser {
            private static final String Marker_ClassDependency = " -> ";

            public JDeps9Parser(IListener listener) {
                super(listener);
            }

            @Override
            public void parseLine(String line) {
                // For example:
                // "   com.google.common.io.BaseEncoding -> com.google.common.io.ByteSink   guava-28.0-jre.jar"
                // "   com.google.common.io.BaseEncoding -> java.lang.Class                 java.base"
                int index = line.indexOf(Marker_ClassDependency);
                if (index > 0) {
                    String className = line.substring(0, index).trim();
                    if (!className.equals(currentClassName)) {
                        onClass(className);
                    }
                    index += Marker_ClassDependency.length();
                    onClassDependency(line.substring(index, line.indexOf(" ", index)));
                }
            }
        }

        /**
         * Parses the given jdeps verbose:class output.
         * 
         * @param jdepsOutput
         *            jdeps verbose:class output.
         * @param listener
         *            Parse listener.
         * @throws IOException
         */
        public static void parse(InputStream jdepsOutput, IListener listener) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(jdepsOutput));
            JDepsClassOutputParser parser = null;
            String line = null;

            // jdeps verbose:class output format detection.
            while ((line = reader.readLine()) != null) {
                // Version 8 class section?
                // For example:
                // "   com.google.common.io.BaseEncoding (guava-28.0-jre.jar)"
                if (line.indexOf(JDeps8Parser.Marker_Class) > 0) {
                    parser = new JDeps8Parser(listener);
                    break;
                } else if (line.indexOf(JDeps9Parser.Marker_ClassDependency) > 0) {
                    // Version 9 class section?
                    // For example:
                    // "   com.google.common.io.BaseEncoding -> com.google.common.io.ByteSink   guava-28.0-jre.jar"
                    if (line.startsWith(" ")) {
                        parser = new JDeps9Parser(listener);
                        break;
                    }
                } else
                    throw new IllegalArgumentException(
                            "jdeps verbose:class output UNKNOWN:\n" + line);
            }
            if (parser == null)
                throw new IllegalArgumentException("jdeps verbose:class output format UNHANDLED");

            // Parsing...
            do {
                parser.parseLine(line);
            } while ((line = reader.readLine()) != null);
            parser.close();
        }

        protected IListener listener;

        protected String currentClassName;

        public JDepsClassOutputParser(IListener listener) {
            super();
            this.listener = listener;
        }

        @Override
        public void close() {
            end();
        }

        public abstract void parseLine(String line);

        protected void end() {
            onClass(null);
        }

        protected void onClass(String className) {
            if (currentClassName != null) {
                listener.onClassEnd(this, currentClassName);
            }
            currentClassName = className;
            if (currentClassName != null) {
                listener.onClassStart(this, currentClassName);
            }
        }

        protected void onClassDependency(String dependencyName) {
            /*
             * NOTE: Default dependencies are purposely ignored.
             */
            if (dependencyName.startsWith("java.") || dependencyName.startsWith("javax."))
                return;

            listener.onClassDependency(this, dependencyName);
        }
    }

    private static final boolean IsWindows = System.getProperty("os.name").startsWith("Windows");

    /**
     * Extracts class dependency mapping from given jdeps output.
     * 
     * @param jdepsOutput
     *            jdeps verbose:class output.
     * @author stechio (https://github.com/stechio)
     * @throws IOException
     */
    public static Map<String, String[]> buildDependencyMap(InputStream jdepsOutput)
            throws IOException {
        Map<String, String[]> map = new HashMap<>();
        JDepsClassOutputParser.parse(jdepsOutput, new JDepsClassOutputParser.IListener() {
            List<String> dependencies = new ArrayList<>();

            @Override
            public void onClassDependency(JDepsClassOutputParser source, String dependencyName) {
                dependencies.add(dependencyName);
            }

            @Override
            public void onClassEnd(JDepsClassOutputParser source, String className) {
                if (!dependencies.isEmpty()) {
                    map.put(className, dependencies.toArray(new String[dependencies.size()]));
                }
            }

            @Override
            public void onClassStart(JDepsClassOutputParser source, String className) {
                dependencies.clear();
            }
        });
        return map;
    }

    /**
     * Extracts class dependency mapping from given jar.
     * 
     * @param jdeps
     *            Path to jdeps utility.
     * @param jar
     *            Path to jar (file or uncompressed folder).
     * @author Deconimus
     * @author stechio (https://github.com/stechio)
     * @throws IOException
     */
    public static Map<String, String[]> buildDependencyMap(String jdeps, File jar)
            throws IOException {
        ProcessBuilder pb;
        if (IsWindows) {
            pb = new ProcessBuilder(jdeps, "-verbose:class", "-filter:none",
                    "\"" + jar.getAbsolutePath() + "\"");
        } else {
            pb = new ProcessBuilder("sh", "-c",
                    "jdeps -verbose:class -filter:none \"" + jar.getAbsolutePath() + "\"");
        }
        Process p = pb.start();
        return buildDependencyMap(p.getInputStream());
    }

    public static void removeRedundantClasses(File dir, File root, Set<String> dependencies,
            String packageName) {
        if (packageName.equalsIgnoreCase("org.eclipse.jdt.internal"))
            return;

        for (File f : dir.listFiles()) {
            if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".class")) {
                String className = packageName + f.getName();
                className = className.substring(0, className.lastIndexOf('.')).trim();

                if (!dependencies.contains(className)) {
                    f.delete();
                }
            } else if (f.isDirectory()) {
                removeRedundantClasses(f, root, dependencies, packageName + f.getName() + ".");
                if (f.list().length <= 0) {
                    f.delete();
                }
            }
        }
    }

    public static void removeRedundantClasses(File dir, Set<String> dependencies) {
        removeRedundantClasses(dir, dir, dependencies, "");
    }
}
