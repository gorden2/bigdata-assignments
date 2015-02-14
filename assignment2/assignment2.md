<h2>Assignment 2</h2>

<h3>Question 0. Briefly describe in prose your solution, both the pairs and stripes implementation.</h3>
<ul>
<li>Pairs: I have two mapreduce jobs. The first one is a word count job that counts the appearance of words. The second job is another word count job which counts the appearance of pairs. Then I used the output of the first job as side data and stored in a HashMap. In the mapping task of this job, it emits the (pair,value) as the intermediate pairs. In the reduce task of the second job, I ued the side data as a dictionary to compute the PMI.
<p>The input of both jobs are the set of the initial input.</p>
</li>
<li>Stripes: I have two mapreduce jobs. The first one is a word count job that counts the appearance of words. The second job is another word count job which counts the appearance of pairs. Then I used the output of the first job as side data and stored in a HashMap. In the mapping task of this job, it emits the (word,hashmap) as the intermediate pairs. In the reduce tasks of the second job, I ued the side data as a dictionary to compute the PMI.
<p>The input of both jobs are the set of the initial input.</p>
</ul>

<h3>bible+shakes data</h3>
<h3>Question 1. What is the running time of the complete pairs implementation? What is the running time of the complete stripes implementation?</h3> 
<p>I used the VM to run the code. It took <strong>141.243s</strong> to run pairs with combiner and <strong>85.013s</strong> to run stripes with combiner.</p>

<h3>Question 2. Now disable all combiners. What is the running time of the complete pairs implementation now? What is the running time of the complete stripes implementation? </h3>
<p>I used the VM to run the code. It took <strong>165.901s</strong> to run pairs without combiner and <strong>103.057s</strong> to run stripes without combiner.</p>


<h3>Question 3. How many distinct PMI pairs did you extract?</h3>
<p>116759</p>

<h3>Question 4. What's the pair (x, y) with the highest PMI? Write a sentence or two to explain what it is and why it has such a high PMI.</h3>
<p>It is <strong>shadrach;abednego: -1.14612803568</strong>. The pair is two people's names in Bible. I think this may because these two people have a lot of common stories.(I am not familiar with Bible)</p>

<h3>Question 5. What are the three words that have the highest PMI with 'cloud' and 'love'? And what are the PMI values?</h3>
<p>They are:
<strong><ul>
<li>cloud; tabernacle: -3.39008687542</li>
<li>cloud; glory: -3.71760999102</li>
<li>cloud; fire: -3.78857488982</li>
<li>love; hate: -4.07518185462</li>
<li>love; hermia: -4.31254277041</li>
<li>love; commandments: -4.35138826656</li>
</ul></strong></p>

<h3>Wiki Data</h3>
<h3>Question 1. What is the running time of the complete pairs implementation? What is the running time of the complete stripes implementation?</h3> 
<p>It took <strong>25942.074s</strong> to run pairs with combiner and <strong>5227.513s</strong> to run stripes with combiner.</p>

<h3>Question 2. Now disable all combiners. What is the running time of the complete pairs implementation now? What is the running time of the complete stripes implementation? </h3>
<p>I used the VM to run the code. It took <strong>infinite s</strong> to run pairs without combiner and <strong></strong> to run stripes without combiner.</p>

<h3>Question 3. How many distinct PMI pairs did you extract?</h3>
<p>19105212</p>

<h3>Question 4. What's the pair (x, y) with the highest PMI? Write a sentence or two to explain what it is and why it has such a high PMI.</h3>
<p>It is <strong>départments,;départment):-1.0</strong>. The two terms look similar and both have symbols. The second one is the singular form of the first one. So it is highly possible that they appears together in a sentence to list department.</p>

<h3>Question 5. What are the three words that have the highest PMI with 'cloud' and 'love'? And what are the PMI values?</h3>
<p>They are:
<strong><ul>
<li>cloud; nebula.: -2.53609911468</li>
<li>cloud; cloud.: -2.58112608449</li>
<li>cloud; thunderstorm: -2.78887511578</li>
<li>love; madly: -3.04844180355</li>
<li>love; hurries: -3.16238515586</li>
<li>love; potion: -3.18674450172</li>
</ul></strong></p>