import re

f = open("cooccur.all",'r')
lines = f.readlines();
print "Question 3. How many distinct PMI pairs did you extract?"
print len(lines)/2
data ={}
for line in lines:
	maps = line.split()
	data[maps[0]]=float(maps[1])

f.close()

print "Question 4. What's the pair (x, y) with the highest PMI? Write a sentence or two to explain what it is and why it has such a high PMI."
q4 = max(data.iterkeys(), key=lambda k: data[k])
print q4 + ": "+ str(data[q4])


print "Question 5. What are the three words that have the highest PMI with 'cloud' and 'love'? And what are the PMI values?"
cloud = {}
love = {}
for key in data:
	word = key.split(";")
	if word[0] == "cloud":
		cloud[word[1]] = data[key]
	if word[0] == "love":
		love[word[1]] = data[key]

for i in range(0,3):
	cq5 = max(cloud.iterkeys(), key=lambda k: cloud[k])
	print "cloud; "+ cq5 + ": "+ str(cloud.pop(cq5))
	lq5 = max(love.iterkeys(), key=lambda k: love[k])
	print "love; "+ lq5 + ": "+ str(love.pop(lq5))
	
