import os
import json
import importlib.util
import shutil


path = os.path.dirname(os.path.abspath(__file__)).replace("\\", "/")


def main():
	
	if not os.path.exists(path+"/build.json"):
		print("\""+path+"build.json\" not found.")
		return
		
	data = None
		
	with open(path+"/build.json") as f:
		data = json.load(f)
	
	if data is None or len(data) <= 0:
		print("build.json is corrupted.")
		return
		
	builderDir = None
	
	if "jarBuilder" in data and "path" in data["jarBuilder"]:
		
		builderDir = os.path.abspath(data["builder"]["path"]).replace("\\", "/")
	else:
		builderDir = "jarBuilder"
	
	buildScriptFile = builderDir+"/jarBuilder.py"
	
	if not os.path.exists(buildScriptFile):
		print("\""+buildScriptFile+"\" not found.")
		return
		
	buildScriptSpec = importlib.util.spec_from_file_location("jarBuilder", buildScriptFile)
	buildScript = importlib.util.module_from_spec(buildScriptSpec)
	buildScriptSpec.loader.exec_module(buildScript)
	
	
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
		srcDirs = [path+"/src"]
		
	if not jarName.lower().endswith(".jar"):
		jarName = jarName+".jar"
	
	completePaths(srcDirs)
	completePaths(imports)
	completePaths(dynImports)
	completePaths(dynImportsExt)
	extLibDir = completePath(extLibDir)
	completePaths(packFiles)
	outDir = completePath(outDir)
	
	
	print("Building")
	
	buildScript.build(path, jarName, outDir, mainClass, srcDirs, imports, 
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
			
		jarShrinkTmp = path+"/jarShrink_tmp"
		
		print("Shrinking")
		
		os.system("java -jar \""+jarShrink+"\" "+jp+" -out "+jp+" -t \""+jarShrinkTmp+"\" -n "+ks)
		
		shutil.rmtree(jarShrinkTmp)
		
	print("Done")


def completePaths(paths):
	
	for i in range(0, len(paths)):
		
		paths[i] = completePath(paths[i])
			
			
def completePath(p):
	
	p = p.replace("\\", "/")
	
	if not p.startswith("/") and not (len(p) > 1 and p[1] == ":"):
		
		return path+"/"+p
		
	return p
	
	
if __name__ == "__main__":
	
	main()