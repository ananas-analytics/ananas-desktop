
echo "Testing the first step (deprecated "
#curl -X POST -i http://localhost:3001/v1/pipeline/5b68874f10987111fbdc8d81/step/5b68874f10987111fbdc8d82/test

#echo "Testing the whole pipeline"
curl -X POST -i http://localhost:3003/v1/pipeline/5b68874f10987111fbdc8d81/test

