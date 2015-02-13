import re

f = open("pairs.all",'r')
lines = f.readlines();
print "Question 3. How many distinct PMI pairs did you extract?"
print len(lines)/2
data ={}
for line in lines:
	maps = line.split("\t")
	data[maps[0]]=float(maps[1])

f.close()

print "Question 4. What's the pair (x, y) with the highest PMI? Write a sentence or two to explain what it is and why it has such a high PMI."
q4 = max(data.iterkeys(), key=lambda k: data[k])
print q4 + ": "+ str(data[q4])


	
