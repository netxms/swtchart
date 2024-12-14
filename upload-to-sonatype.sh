#!/bin/sh

set -e

if [ -z $1 ]; then
   V=$(xq -r .project.version swtchart/pom.xml)
else
   V=$1
fi

echo Target version: $V
echo Continue?
read

mvn clean package

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -Possrh -DpomFile=swtchart/pom.xml -Dpackaging=jar -Dfile=swtchart/target/swtchart-$V.jar
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -Possrh -DpomFile=swtchart/pom.xml -Dpackaging=jar -Dfile=swtchart/target/swtchart-$V-sources.jar -Dclassifier=sources
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -Possrh -DpomFile=swtchart/pom.xml -Dpackaging=jar -Dfile=swtchart/target/swtchart-$V-javadoc.jar -Dclassifier=javadoc

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -Possrh -DpomFile=rwtchart/pom.xml -Dpackaging=jar -Dfile=rwtchart/target/rwtchart-$V.jar
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -Possrh -DpomFile=rwtchart/pom.xml -Dpackaging=jar -Dfile=rwtchart/target/rwtchart-$V-sources.jar -Dclassifier=sources
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -Possrh -DpomFile=rwtchart/pom.xml -Dpackaging=jar -Dfile=rwtchart/target/rwtchart-$V-javadoc.jar -Dclassifier=javadoc
