#!/usr/bin/env bash

bold=$(tput bold)
normal=$(tput sgr0)

sessionactive=1		# Keeps track if user is still deleting files
keeppath=0			# Determines if user wants to keep path across deletions
validsearch=0		# Determines if query exists in filesystem
validpath=0			# Determmines if path exists in filesystem
numfilesfound=0		# Keeps track of number of files found
savecontinue=0		# Keeps track if user would like to continuously delete with no prompt
savekeeppath=0		# Keeps track if user would like to continue searching the initial path

reset() 
{
	numfilesfound=0
	validpath=0
	validsearch=0
	deletepath=""
	filename=""
}

: '
Catch-all function that handles all user-input
scenarios
'
userInputHandler() 
{
	# Handle empty input
	if [[ -z "$1" ]]; then
		printf ": No input entered. Try again.\n\n"

	# Handle quit
	elif [[ "$1" == "quit" ]]; then
		printf ": Exiting...\n"
		exit 0

	# Handle search query validation
	elif [[ $2 == "search" ]]; then
		if  [[ $1 == "back" ]]; then
			pathprompt
		elif [[ -n $(find "$initpath" -type f -iname "*$1*") ]]; then
			validsearch=1															# Set search as valid
			numfilesfound="$(find "$initpath" -type f -iname "*$1*" | wc -l)"		# Count number of files found
			echo "Nums: $numfilesfound"
			deletepath="$(find "$initpath" -type f -iname "*$1*" -printf "%h\n")"	# Save valid path
			filename="$(find "$initpath" -type f -iname "*$1*" -printf "%f\n")"		# Save full filename
		else
			printf ": File not found when searching. Try again.\n\n"
		fi

	# Handle path validation
	elif [[ $2 == "path" ]]; then					
		if [[  -d "$1" ]]; then
			validpath=1
		else	
			printf ": Path not found. Try again.\n\n"
		fi

	# Handle deletion choice
	elif [[ $2 == "delete" ]]; then	
		case $1 in
			Y | y)
				$(rm "$deletepath"/"$filename")
				case $? in
					0)
						printf ": %s successfully deleted.\n\n" "$filename"
					;;
					*)
						printf ": %s failed to delete. Error code %d.\n\n" "$filename" "$?"
					;;
				esac
				continuesession
			;;
			N | n)
				printf ": Not deleting ${bold}%s ${normal}\n\n" "$filename"
				continuesession
			;;
			"back")
				queryprompt
			;;
			*)
				printf ": Invalid choice. Try again.\n\n"
				deletefile
			;;
		esac

	# Handle continue choice
	elif [[ $2 == "continue" ]] && [[ $savecontinue == 0 ]]; then	
		case $1 in
			Y | y)
				keeppath
			;;
			N | n)
				sessionactive=0
				echo ": Successfully stopped session."
			;;
			"back")
				deletefile
			;;
			*)
				 printf ": Invalid choice. Try again.\n\n"
 	             continuesession
			;;
		esac
	
	# Handle keep path choice
	elif [[ $2 == "keep" ]]; then
		validsearch=0
		case $1 in
			Y | y)
				echo ": Continuing to search path $initpath"
			;;
			N | n)
				printf "::: Please specify a new path ('quit' to exit, 'back' to go back): "
				read initpath
				userInputHandler $initpath "path"
			;;
			"back")
				continuesession
			;;
			*)
				printf ": Invalid choice. Try again.\n\n"
				keeppath
			;;
		esac
	fi
}

deletefile() 
{
	# Only allow 1 file deletetion at a time
	if [[ $numfilesfound != 1 ]]; then
		echo ": Search not specific enough, more than one file found. Try again."
		queryprompt
	fi

	# Show path to user to confirm/deny deletion for each file found
	printf ": Delete: %s in %s? ('quit' to exit, 'back' to go back) [Y/N]: " "$filename" "$deletepath"
	read deletechoice
	userInputHandler "$deletechoice" "delete"
}

continuesession() 
{
	# Ask to continue deletion session
	printf "::: Continue session? ('quit' to exit, 'back' to go back) [Y/N]: "
	read continuechoice
	userInputHandler $continuechoice "continue"
}

keeppath() 
{
	validsearch=0
	printf "Keep searching specified path? ('quit' to exit, 'back' to go back) [Y/N]: "
	read keepchoice
	userInputHandler $keepchoice "keep"
}

pathprompt() 
{
	printf "::: Path to search (E.g. /, /home/, etc.) ('quit' to exit)?: "
	read initpath
	userInputHandler "$initpath" "path"	
}

queryprompt() 
{
	printf "::: Enter the name of the file you would like to delete ('quit' to exit, 'back' to go back): "
	read query
	userInputHandler "$query" "search"
}

saveprompt() 
{
	printf "::: Save your choice? ('quit' to exit, 'back' to go back): "
	read savechoice
	userInputHandler "$savechoice" "save"
}

# While session is active, continue to prompt user to delete files
while [[ $sessionactive -eq 1 ]]; do	
	
	# While path is blank or is not valid, keep looping unless user wants to quit
	while [[ $validpath -eq 0 ]]; do
		pathprompt
	done

	# While query is blank or DNE, keep looping unless user wants to quit	
	while [[ $validsearch -eq 0 ]]; do
		queryprompt
	done

	# Show amount of files found
	echo ": Found $numfilesfound file(s) matching your search."

	
	# Start delete file routine
	deletefile
done
exit 0
