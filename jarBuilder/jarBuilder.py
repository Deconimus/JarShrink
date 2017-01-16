import os
import shutil
import zipfile


path = os.path.dirname(os.path.abspath(__file__)).replace("\\", "/")


def build():
	
	projectPath = path

	jarName = "mangaddict.jar"
	
	mainClass = "main.Main"
	
	srcDirs = [projectPath+"/src"]
	classPaths = []
	
	dynamicIncludes = []
	dynamicLinks = []
	
	packFiles = ["LICENSE", "README.md"]
	
	extLibDir = "lib"

	outdir = projectPath

	build(projectPath, jarName, outdir, mainClass, srcDirs, classPaths, 
		  dynamicIncludes, dynamicLinks, extLibDir, packFiles)
	

def build(projectPath, jarName, outdir, mainClass, srcDirs, classPaths, 
		  dynamicIncludes, dynamicLinks, extLibDir, packFiles):
	
	tmp = path+"/tmp"
	
	os.chdir(projectPath)
	
	if os.path.exists(tmp):
		
		shutil.rmtree(tmp)
	
	os.makedirs(tmp)
	
	for f in packFiles:
		shutil.copy(f, tmp+"/"+(f.split("/")[-1]))
		
	copyClassFiles(classPaths, tmp)
	copyLibraries(dynamicIncludes, tmp)
	copyLibraries(dynamicLinks, outdir+"/"+extLibDir)
	
	if len(dynamicIncludes) > 0:
		copyClassFiles([path+"/res/classloader"])
	
	srcList = sourceListString(getPackages(srcDirs, projectPath))
	srcPath = " -sourcepath "+pathList(srcDirs) if len(srcDirs) > 0 else ""
	clsPath = " -cp "+pathList(classPaths+dynamicIncludes+dynamicLinks) if len(classPaths) > 0 else ""
	
	
	os.system("javac "+srcList+""+clsPath+""+srcPath+" -d \""+tmp+"/\"")
	
	createManifest(projectPath, dynamicIncludes, dynamicLinks, extLibDir, mainClass)
	
	os.chdir(tmp)
	os.system("jar cmf \""+projectPath+"/MANIFEST.MF\" \""+outdir+"/"+jarName+"\" *")
	
	os.chdir(projectPath)
	
	shutil.rmtree(tmp)
	os.remove(projectPath+"/MANIFEST.MF")
	
	
def getPackages(sourceDirs, projectPath):
	
	files = []
	
	for sourceDir in sourceDirs:
	
		for dirName, subdirList, fileList in os.walk(sourceDir):
			
			for f in fileList:
				if (f.lower().endswith(".java")):
					
					name = dirName.replace("\\", "/")
					
					if name.startswith(projectPath):
						name = name[len(projectPath)+1:]
						
					files.append(name)
					break
				
	return files
	
	
def sourceListString(sourceFiles):
	
	str = ""
	
	for f in sourceFiles:
		
		str = str+"\""+f+"/\"*.java "
		
	return str.strip()
	
	
def pathList(paths):
	
	if len(paths) <= 0:
		return ""
	
	str = ""
	
	for p in paths:
		
		str = str+"\""+p
		
		if not p.lower().endswith(".jar"):
			str = str+"/"
			
		str = str+"\""+os.pathsep
		
	return str[:-1]
	
	
def copyClassFiles(paths, tmp):
	
	for d in paths:
		
		if d.lower().endswith(".jar"):
			
			#shutil.copy(d, tmp+"/"+d.replace("\\", "/").split("/")[-1])
			
			with zipfile.ZipFile(d, "r") as jar:
				
				for entry in jar.infolist():
					
					if entry.filename.endswith("/") or entry.filename.endswith("MANIFEST.MF"):
						continue
						
					outFile = (tmp+"/"+entry.filename)
					outDir = outFile[:outFile.rindex("/")]
						
					if not os.path.exists(outDir):
						os.makedirs(outDir)
						
					jar.extract(entry, path=tmp)
			
		else:
			
			for dirName, subdirList, fileList in os.walk(d):
				
				for f in fileList:
					if (f.lower().endswith(".class")):
						
						name = dirName.replace("\\", "/")+"/"+f
						
						if not os.path.exists(tmp+"/"+dirName[len(d)+1:]):
							os.makedirs(tmp+"/"+dirName[len(d)+1:])
						
						shutil.copy(name, tmp+"/"+name[len(d)+1:])
	
	
def copyLibraries(paths, parent):
	
	if len(paths) > 0 and not parent is None and not os.path.exists(parent):
		os.makedirs(parent)
	
	for f in paths:
		
		if f.lower().endswith(".jar"):
			
			shutil.copy(f, parent+"/"+f.replace("\\", "/").split("/")[-1])
			
	
def createManifest(projectPath, dynamicIncludes, dynamicLinks, extLibDir, mainClass):
	
	txt = "Manifest-Version: 1.0\n"
	
	if len(dynamicIncludes) > 0:
		
		txt = txt+"Rsrc-Class-Path: ./"
		for lib in dynamicIncludes:
			txt = txt+" "+lib.replace("\\", "/").split("/")[-1]
		txt = txt+"\n"
	
	txt = txt + "Class-Path: ."
	for lib in dynamicLinks:
		txt = txt+" \""+extLibDir+"/"+lib.replace("\\", "/").split("/")[-1]+"\""
	txt = txt + "\n"
	
	if len(mainClass) > 0:
	
		txt = txt + ("Rsrc-" if len(dynamicIncludes) > 0 else "") + "Main-Class: "+mainClass.strip()+"\n";
		
		if len(dynamicIncludes) > 0:
			
			txt = txt + "Main-Class: org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader\n"
	
	with open(projectPath+"/MANIFEST.MF", "w+") as f:
		
		f.write(txt)
	
	
if __name__ == "__main__":
	
	build()