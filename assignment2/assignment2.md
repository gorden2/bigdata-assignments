<h2>Assignment 2</h2>

<h3>Question 0. Briefly describe in prose your solution, both the pairs and stripes implementation.</h3>
<ul>
<li>Pairs: I have two mapreduce jobs. The first one is a word count job that counts the appearance of words. The second job is another word count job which counts the appearance of pairs. Then I used the output of the first job as side data and stored in a HashMap. In the mapping task of this job, it emits the (pair,value) as the intermediate pairs. In the reduce task of the second job, I ued the side data as a dictionary to compute the PMI.
<p>The input of both jobs are the set of the initial input.</p>
</li>
<li>Stripes: I have two mapreduce jobs. The first one is a word count job that counts the appearance of words. The second job is another word count job which counts the appearance of pairs. Then I used the output of the first job as side data and stored in a HashMap. In the mapping task of this job, it emits the (word,hashmap) as the intermediate pairs. In the reduce tasks of the second job, I ued the side data as a dictionary to compute the PMI.
<p>The input of both jobs are the set of the initial input.</p>
</ul>

<strong>Note: In order to simplify the PMI calculation(Actually, I am not sure about the definition), I used the count of elements as its probability. The acutally value of PMI is just the value I displayed plus log10(N), where N is a constant value.<strong>

<h3>bible+shakes data</h3>
<h3>Question 1. What is the running time of the complete pairs implementation? What is the running time of the complete stripes implementation?</h3> 
<p>I used the VM to run the code. It took <strong>141.243s</strong> to run pairs with combiner and <strong>85.013s</strong> to run stripes with combiner.</p>

<h3>Question 2. Now disable all combiners. What is the running time of the complete pairs implementation now? What is the running time of the complete stripes implementation? </h3>
<p>I used the VM to run the code. It took <strong>165.901s</strong> to run pairs without combiner and <strong>103.057s</strong> to run stripes without combiner.</p>


<h3>Question 3. How many distinct PMI pairs did you extract?</h3>
<p>116759</p>

<h3>Question 4. What's the pair (x, y) with the highest PMI? Write a sentence or two to explain what it is and why it has such a high PMI.</h3>
<p>It is <strong>shadrach;abednego: 4.04759469747</strong>. The pair is two people's names in Bible. I think this may because these two people have a lot of common stories.(I am not familiar with Bible)</p>

<h3>Question 5. What are the three words that have the highest PMI with 'cloud' and 'love'? And what are the PMI values?</h3>
<p>They are:
<strong><ul>
<li>cloud; tabernacle: 1.80363585773</li>
<li>cloud; glory: 1.47611274213</li>
<li>cloud; fire: -3.78857488982</li>
<li>love; hate: 1.40514784333</li>
<li>love; hermia: 0.88117996274</li>
<li>love; commandments: 0.84233446659</li>
</ul></strong></p>

<h3>Wiki Data</h3>
<h3>Question 6. What is the running time of the complete pairs implementation? What is the running time of the complete stripes implementation?</h3> 
<p>It took <strong>25942.074s</strong> to run pairs with combiner and <strong>5227.513s</strong> to run stripes with combiner.</p>

<h3>Question 7. Now disable all combiners. What is the running time of the complete pairs implementation now? What is the running time of the complete stripes implementation? </h3>
<p>I used the VM to run the code. It took <strong>22200.217s</strong> to run pairs without combiner and <strong>2683.866s</strong> to run stripes without combiner.</p>

<h3>Question 8. How many distinct PMI pairs did you extract?</h3>
<p>19105212</p>

<h3>Question 9. What's the pair (x, y) with the highest PMI? Write a sentence or two to explain what it is and why it has such a high PMI.</h3>
<p>It is <strong>départments,;départment):4.05949904516</strong>. The two terms look similar and both have symbols. The second one is the singular form of the first one. So it is highly possible that they appears together in a sentence to list department.</p>
5.05949904516
<h3>Question 10. What are the three words that have the highest PMI with 'cloud' and 'love'? And what are the PMI values?</h3>
<p>They are:
<strong><ul>
<li>cloud; nebula.: 2.52339993048</li>
<li>cloud; cloud.: 2.47837296067</li>
<li>cloud; thunderstorm: 2.27062392938</li>
<li>love; madly: 2.01105724161</li>
<li>love; hurries: 1.8971138893</li>
<li>love; potion: 1.87275454344</li>
</ul></strong></p>
