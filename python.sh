#!/bin/bash -l
###############
# Python executor in HPC
# Give a executable python file, then this file execute it on the virtual environment of python3
# The default virtual environment is "~/venv/bin/python3"
# If you want to change it, Please specify "-v" parameter with the path of the virtual environment

DRY_RUN=0
PARAMS=""
VENV_PATH=~/projects/OPAM/venv/bin/python3

# Parse the command-line argument
while [ $# -ge 1 ]; do
    case $1 in
        -h | --help) usage; exit 0;;
        -d | --dry-run) DRY_RUN=1;;
        -v | --virtual) VENV_PATH=$2; shift;;
        *) PARAMS="$*"; break; ;;
    esac
    shift;
done


if [ "$PARAMS" == "" ]; then
  echo "Error:: Not specified cmd parameters";
  exit
fi



# Load modules
# Execute
echo ${VENV_PATH} ${PARAMS}
if [ $DRY_RUN -eq 0 ]; then
  ${VENV_PATH} ${PARAMS}
fi