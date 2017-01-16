import os
import json
import importlib.util


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
		
	jarCreatorFile = builderDir+"/jarCreator.py"
	
	if not os.path.exists(buildScriptFile):
		print("\""+jarCreatorFile+"\" not found.")
		return
		
	buildScript = loadModule("jarBuilder", buildScriptFile)
	
	buildScript.build(path, data, loadModule("jarCreator", jarCreatorFile))
	
	
def loadModule(moduleName, moduleFile):
	
	spec = importlib.util.spec_from_file_location(moduleName, moduleFile)
	module = importlib.util.module_from_spec(spec)
	spec.loader.exec_module(module)
	
	return module


if __name__ == "__main__":
	
	main()