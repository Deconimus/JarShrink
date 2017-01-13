import os
import shutil


path = os.path.dirname(os.path.abspath(__file__)).replace("\\", "/")


jarName = "jarShrink.jar"

srcFilePath = path+"/src"

mainClass = "jarshrink.Main"
copyFiles = ["LICENSE", "README.md"]


tmp = path+"/tmp"


def build():
	
	if os.path.exists(tmp):
		
		shutil.rmtree(tmp)
	
	os.makedirs(tmp)
	
	for f in copyFiles:
		shutil.copy(path+"/"+f, tmp+"/"+f)
	
	srcListString = sourceListString(getSourceFiles(srcFilePath))
	
	os.chdir(path)
	os.system("javac "+srcListString+" -sourcepath \""+srcFilePath+"/\" -d \""+tmp+"/\"")
	
	with open(path+"/MANIFEST.MF", "w+") as f:
		
		f.write("Manifest-Version: 1.0\nClass-Path: .\nMain-Class: "+mainClass+"\n")
		
	os.chdir(tmp)
	os.system("jar cmf \"../MANIFEST.MF\" \"../"+jarName+"\" *")
	
	os.chdir(path)
	
	shutil.rmtree(tmp)
	os.remove(path+"/MANIFEST.MF")
	
	
def getSourceFiles(directory):
	
	files = []
	
	for dirName, subdirList, fileList in os.walk(directory):
		
		for f in fileList:
			
			if (f.lower().endswith(".java")):
			
				files.append(os.path.join(dirName, f)[len(path)+1:])
				
	return files
	
	
def sourceListString(sourceFiles):
	
	str = ""
	
	for f in sourceFiles:
		
		str = str + "\""+f+"\" "
		
	return str.strip()
	
	
if __name__ == "__main__":
	
	build()