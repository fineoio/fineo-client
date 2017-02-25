#!/bin/bash
# Update the doc version in the docs/ directory to the specified version

if [ $# -ne 1 ]; then
  echo "Must supply the new version as the first argument"
  exit 0
fi

version=$1
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -x
cd $DIR/docs
# replace the version
find ./ -type f -exec sed -i -e "s/|version|/${version}/g" {} \;
# clean and extra docs
git clean -f

echo "|version| updated to $version"
