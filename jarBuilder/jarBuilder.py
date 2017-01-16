import os
import shutil


def build(projectPath, data, jarCreator):
	
	jarName = data["jarName"] if "jarName" in data else "app"
	outDir = data["outDir"] if "outDir" in data else ""
	mainClass = data["main"] if "main" in data else ""
	srcDirs = data["sourceDirs"] if "sourceDirs" in data else None
	imports = data["imports"] if "imports" in data else []
	dynImports = data["dynImports"] if "dynImports" in data else []
	dynImportsExt = data["dynImportsExt"] if "dynImportsExt" in data else []
	extLibDir = data["extLibDir"] if "extLibDir" in data else "lib"
	packFiles = data["packFiles"] if "packFiles" in data else []
	
	if srcDirs is None or len(srcDirs) <= 0:
		srcDirs = [projectPath+"/src"]
		
	if not jarName.lower().endswith(".jar"):
		jarName = jarName+".jar"
	
	completePaths(srcDirs, projectPath)
	completePaths(imports, projectPath)
	completePaths(dynImports, projectPath)
	completePaths(dynImportsExt, projectPath)
	extLibDir = completePath(extLibDir, projectPath)
	completePaths(packFiles, projectPath)
	outDir = completePath(outDir, projectPath)
	
	
	print("Building")
	
	jarCreator.build(projectPath, jarName, outDir, mainClass, srcDirs, imports,
					 dynImports, dynImportsExt, extLibDir, packFiles)
	
	
	jarShrink = None
	jarShrinkKeep = []
	
	if "jarShrink" in data and "path" in data["jarShrink"]:
		
		jarShrink = data["jarShrink"]["path"]
		
		if not os.path.exists(jarShrink):
			print("\""+jarShrink+"\" not found.")
			jarShrink = None
		
		if "keep" in data["jarShrink"]:
			
			jarShrinkKeep = data["jarShrink"]["keep"]
		
			
	if not jarShrink is None:
		
		jp = "\""+outDir+"/"+jarName+"\""
		
		ks = ""
		for k in jarShrinkKeep:
			ks = ks+" -k \""+k+"\""
			
		jarShrinkTmp = projectPath+"/jarShrink_tmp"
		
		print("Shrinking")
		
		os.system("java -jar \""+jarShrink+"\" "+jp+" -out "+jp+" -t \""+jarShrinkTmp+"\" -n "+ks)
		
		shutil.rmtree(jarShrinkTmp)
		
	print("Done")


def completePaths(paths, projectPath):
	
	for i in range(0, len(paths)):
		
		paths[i] = completePath(paths[i], projectPath)
			
			
def completePath(p, projectPath):
	
	p = p.replace("\\", "/")
	
	if not p.startswith("/") and not (len(p) > 1 and p[1] == ":"):
		
		return projectPath+"/"+p
		
	return p