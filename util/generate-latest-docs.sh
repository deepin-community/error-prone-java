# Copyright 2020 The Error Prone Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# https://github.com/google/dagger/blob/master/util/generate-latest-docs.sh

set -eu

echo -e "Publishing docs...\n"

GH_PAGES_DIR=$HOME/gh-pages

git clone --quiet --branch=gh-pages https://x-access-token:${GITHUB_TOKEN}@github.com/google/error-prone.git $GH_PAGES_DIR > /dev/null
(
  cd $GH_PAGES_DIR
  rm -rf _data/bugpatterns.yaml api/latest
  mkdir -p _data api/latest
)

mvn -P '!examples' javadoc:aggregate
rsync -a target/site/apidocs/ ${GH_PAGES_DIR}/api/latest

# The "mvn clean" is necessary since the wiki docs are generated by an
# annotation processor that also compiles the code.  If Maven thinks the code
# does not need to be recompiled, the wiki docs will not be generated either.
mvn clean

mvn -P run-annotation-processor compile site
rsync -a docgen/target/generated-wiki/ ${GH_PAGES_DIR}
# remove docs from deleted checkers
rsync --delete -a docgen/target/generated-wiki/bugpattern/ ${GH_PAGES_DIR}/bugpattern/

cd $GH_PAGES_DIR
git add --all .
git config --global user.name "$GITHUB_ACTOR"
git config --global user.email "$GITHUB_ACTOR@users.noreply.github.com"
if git commit -m "Latest docs on successful build $GITHUB_RUN_NUMBER auto-pushed to gh-pages"; then
    git push -fq origin gh-pages > /dev/null
    echo -e "Published docs to gh-pages.\n"
else
    echo -e "No doc changes to publish.\n"
fi
