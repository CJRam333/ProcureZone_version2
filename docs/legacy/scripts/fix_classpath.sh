#!/bin/bash
# Script to fix the classpath references

cd "e:\Net-Beans\ProcureZone"

# Backup the original file
cp nbproject/project.properties nbproject/project.properties.backup

# Replace all the problematic jar references with direct paths
sed -i 's|D:\\\\E\\\\OldJavaProjects\\\\lib\\\\|web/WEB-INF/lib/|g' nbproject/project.properties
sed -i 's|${web.docbase.dir}/WEB-INF/lib/|web/WEB-INF/lib/|g' nbproject/project.properties

echo "Classpath fixed!"