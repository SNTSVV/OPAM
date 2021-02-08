#!/bin/bash -l
echo "Start date:"
date
echo "$0 $1"
./uppaal64-4.1.24/bin-Linux/verifyta $1
echo "End date:"
date
