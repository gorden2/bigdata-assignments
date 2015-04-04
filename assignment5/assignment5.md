<h2>assignment 5</h2>
<h3>1. What is the scalability issue with this particular HBase table design?</h3>
<p>
Since the column qualifier is the document id, adding new documents needs a scan over all the exsting words. 
</p>
<br/>
<h3>2. How would you fix it? </h3>
<p>Schema Design: </p>
<p>
First, make the "Docid" as the row key. If the document id is the url of the website, then reverse the url. For example: "www.umd.edu" will become "edu.umd.www". 
In that case, similar websites will come together.
</p>
<p>
Then, the word will become the column qualifier. In this case, the retrieval system would scanning the columns rather than the rows.
</p>


