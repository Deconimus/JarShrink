package visionCore.io;

import java.io.PrintStream;
import java.util.Locale;

public class MultiPrintStream extends PrintStream {

	private final PrintStream[] streams;
	
	public MultiPrintStream(PrintStream... streams) {
		super(new MultiOutputStream(streams));
		
		this.streams = new PrintStream[streams.length];
		
		for (int i = 0; i < streams.length; i++) {
			
			this.streams[i] = streams[i];
		}
		
	}
	
	@Override
	public PrintStream append(char c) {
		
		for (PrintStream out : streams) {
			
			out.append(c);
		}
		
		return this;
	}
	
	@Override
	public PrintStream append(CharSequence csq) {
		
		for (PrintStream out : streams) {
			
			out.append(csq);
		}
		
		return this;
	}
	
	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		
		for (PrintStream out : streams) {
			
			out.append(csq, start, end);
		}
		
		return this;
	}
	
	@Override
	public boolean checkError() {
		
		for (PrintStream out : streams) {
			
			if (out.checkError()) { return true; }
		}
		
		return false;
	}
	
	@Override
	public void close() {
		
		for (PrintStream out : streams) {
			
			out.close();
		}
		
	}
	
	@Override
	public void flush() {
		
		for (PrintStream out : streams) {
			
			out.close();
		}
		
	}
	
	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		
		for (PrintStream out : streams) {
			
			out.format(l, format, (Object[])(args));
		}
		
		return this;
	}
	
	@Override
	public PrintStream format(String format, Object... args) {
		
		for (PrintStream out : streams) {
			
			out.format(format, (Object[])(args));
		}
		
		return this;
	}
	
	@Override
	public void print(boolean b) {
		
		for (PrintStream out : streams) {
			
			out.print(b);
		}
		
	}
	
	@Override
	public void print(char c) {
		
		for (PrintStream out : streams) {
			
			out.print(c);
		}
		
	}
	
	@Override
	public void print(char[] s) {
		
		for (PrintStream out : streams) {
			
			out.print(s);
		}
		
	}
	
	@Override
	public void print(double d) {
		
		for (PrintStream out : streams) {
			
			out.print(d);
		}
		
	}
	
	@Override
	public void print(float f) {
		
		for (PrintStream out : streams) {
			
			out.print(f);
		}
		
	}
	
	@Override
	public void print(int i) {
		
		for (PrintStream out : streams) {
			
			out.print(i);
		}
		
	}
	
	@Override
	public void print(long l) {
		
		for (PrintStream out : streams) {
			
			out.print(l);
		}
		
	}
	
	@Override
	public void print(Object obj) {
		
		for (PrintStream out : streams) {
			
			out.print(obj);
		}
		
	}
	
	@Override
	public void print(String s) {
		
		for (PrintStream out : streams) {
			
			out.print(s);
		}
		
	}
	
	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		
		for (PrintStream out : streams) {
			
			out.printf(l, format, (Object[])(args));
		}
		
		return this;
	}
	
	@Override
	public PrintStream printf(String format, Object... args) {
		
		for (PrintStream out : streams) {
			
			out.printf(format, (Object[])(args));
		}
		
		return this;
	}
	
	@Override
	public void println(boolean b) {
		
		for (PrintStream out : streams) {
			
			out.println(b);
		}
		
	}
	
	@Override
	public void println(char c) {
		
		for (PrintStream out : streams) {
			
			out.print(c);
		}
		
	}
	
	@Override
	public void println(char[] s) {
		
		for (PrintStream out : streams) {
			
			out.println(s);
		}
		
	}
	
	@Override
	public void println(double d) {
		
		for (PrintStream out : streams) {
			
			out.println(d);
		}
		
	}
	
	@Override
	public void println(float f) {
		
		for (PrintStream out : streams) {
			
			out.println(f);
		}
		
	}
	
	@Override
	public void println(int i) {
		
		for (PrintStream out : streams) {
			
			out.println(i);
		}
		
	}
	
	@Override
	public void println(long l) {
		
		for (PrintStream out : streams) {
			
			out.println(l);
		}
		
	}
	
	@Override
	public void println(Object obj) {
		
		for (PrintStream out : streams) {
			
			out.println(obj);
		}
		
	}
	
	@Override
	public void println(String s) {
		
		for (PrintStream out : streams) {
			
			out.println(s);
		}
		
	}
	
	@Override
	public void write(byte[] buffer, int off, int len) {
		
		for (PrintStream out : streams) {
			
			out.write(buffer, off, len);
		}
		
	}
	
	@Override
	public void write(int b) {
		
		for (PrintStream out : streams) {
			
			out.write(b);
		}
		
	}
	
}
