# Batch Delete Bash Script
####
Simple script to continuously delete specific files in a Linux filesystem.

## How it works
A flow chart of the process:
<img src="https://github.com/joshrharmon/Projects/raw/master/Bash/flowchart.png" width="1000">
## Features
* Numerous safeguards to prevent accidental deletion
* Ability to go back during all steps in the deletion process
* Utilizes fast find command to search for files
  * Ignores case
  * Will match files that contain any part of the query
    * e.g. FooBar.txt will be found if "oob" is searched

## TODO
- [ ] Add ability to delete multiple files at once if query returns more than one result
- [ ] Add colors to make output more readable
- [ ] Show output of all files deleted in session on exit
