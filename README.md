# TeamGG
Team oriented minecraft pvp suite

## Project Goals

1. **Advanced Game Engine with game logic implementated through modular programming.** 
Managers should offer hooks and data models to modules. 
Modules should be capable of communicating with one another.
The project should strive to make new gametype development as straightforward as possible.

2. **Map.json scripting language.**
Maps need access to a baseline scripting service that allow for map-specific dynamic content.
As an example, a map should be able to provide different spawn points as the match time progresses.
```
"spawns": [
		{ 
			"teams": ["blue"], "x": 54.6, "y": 83.4, "z": 93.4, "yaw": 90,
			"conditions": ["time <= 120"]
		},
		{ 
			"teams": ["blue"], "x": 54.6, "y": 83.4, "z": 93.4, "yaw": 90,
			"conditions": ["time > 120", "time < 240"]
		},
		{ 
			"teams": ["blue"], "x": 54.6, "y": 83.4, "z": 93.4, "yaw": 90,
			"conditions": ["time >= 240"]
		},
		{ 
			"teams": ["yellow"], "x": 54.6, "y": 83.4, "z": 93.4, "yaw": 90,
			"conditions": ["points yellow >= 10"]
		}
	]
  ```
  

## Local Server Setup
 
1. Start with the latest [Spigot](https://www.spigotmc.org/) build. 
 
2. Create a `maps` folder inside of the server and insert a supported TGM map. ([Fracture](http://www.mediafire.com/file/45dlhau44hus4mv/fracture.zip))
 
3. Create a file named `rotation` in the server folder. This is a list of maps that the plugin will automatically cycle to. Put any of your maps on their own line in the file. If you are using Fracture, simply put "Fracture" on the first line and save the file. It's important to know that the name of the map specified in the map.json file is used here, not the folder name.
 
4. (Optional) Install World Edit to make the Teleport Tool work.  
 
5. Start the server.
 
## Developer Tips

1. We use Lombok. Make sure you have the Lombok plugin installed on your preferred IDE.

2. We use maven. Like any other maven project, run `mvn clean install` in the top level folder to generated required libraries.