<h3>Pig #1</h3>
a = load '/shared/tweets2011.txt' using PigStorage('\\t') as (id: chararray, name: chararray, time: chararray, text: chararray);
<br/>
b = foreach a generate text, STRSPLIT(time) as tuple(date1: chararray, date2: chararray, date3: chararray, date4: chararray, date5: chararray, date6: chararray);
<br/>
b1 = FILTER b by tuple_0.date2 == 'Jan' AND (int)tuple_0.date3 >= 23;
<br/>
b2 = FILTER b by tuple_0.date2 == 'Feb' AND (int)tuple_0.date3 <= 8;
<br/>
b0 = UNION b1, b2;
<br/>
c = foreach b0 generate text, CONCAT(tuple_0.date2, tuple_0.date3) as date, SUBSTRING(tuple_0.date4,0,2) as time;
<br/>
c1 = foreach c generate text, REPLACE(date,'Jan','1/') as date, time;
<br/>
c2 = foreach c1 generate text, REPLACE(date,'Feb','2/') as date, time;
<br/>
c3 = foreach c2 generate text, CONCAT(CONCAT(date,' '),time) as term;
<br/>
d = group c3 by term;
<br/>
e = foreach d generate group as date, COUNT(c3) as count;
<br/>
f = order e by date;
<br/>
store f into 'pig1';
<br/>
<h3>Pig #2</h3>
c4 = FILTER c3 by REGEX_EXTRACT_ALL(text, '.*([Ee][Gg][Yy][Pp][Tt]|[Cc][Aa][Ii][Rr][Oo]).*') is not null;
<br/>
d2 = group c4 by term;
<br/>
e2 = foreach d2 generate group as date, COUNT(c4) as count;
<br/>
f2 = order f2 by date;
<br/>
store f2 into 'pig2';
<br/>
<h3>Spark #1</h3>
val data0 = sc.textFile("/shared/tweets2011.txt")
<br/>val data = data0.filter(_.split("\\t").length==4)
<br/>val dataset1 = data.filter(l => (l.split("\\t")(2).split(" ")(1)=="Jan" && l.split("\\t")(2).split(" ")(2).toInt>=23))
<br/>val dataset2 = data.filter(l => (l.split("\\t")(2).split(" ")(1)=="Feb" && l.split("\\t")(2).split(" ")(2).toInt<=8))
<br/>val d1 = dataset1.map(l => "1/"+l.split("\\t")(2).split(" ")(2) + " " +l.split("\\t")(2).split(" ")(3).substring(0,2))
<br/>val d2 = dataset2.map(l => "2/"+l.split("\\t")(2).split(" ")(2) + " " +l.split("\\t")(2).split(" ")(3).substring(0,2))
<br/>val d = d1 ++ d2
<br/>val result = d.map(term => (term,1)).reduceByKey(_ + _)
<br/>val results = result.sortBy(x => (x._1.split(" ")(0),x._1.split(" ")(1)))
<br/>results.saveAsTextFile("result1")
<br/>
<h3>Spark #2</h3>
val data0 = sc.textFile("/shared/tweets2011.txt")
<br/>val data = data0.filter(_.split("\\t").length==4)
<br/>val edata = data.filter(l => l.split("\\t")(3).matches(".*([Ee][Gg][Yy][Pp][Tt]|[Cc][Aa][Ii][Rr][Oo]).*"))
<br/>val edataset1 = edata.filter(l => (l.split("\\t")(2).split(" ")(1)=="Jan" && l.split("\\t")(2).split(" ")(2).toInt>=23))
<br/>val edataset2 = edata.filter(l => (l.split("\\t")(2).split(" ")(1)=="Feb" && l.split("\\t")(2).split(" ")(2).toInt<=8))
<br/>val ed1 = edataset1.map(l => "1/"+l.split("\\t")(2).split(" ")(2) + " " +l.split("\\t")(2).split(" ")(3).substring(0,2))
<br/>val ed2 = edataset2.map(l => "2/"+l.split("\\t")(2).split(" ")(2) + " " +l.split("\\t")(2).split(" ")(3).substring(0,2))
<br/>val ed = ed1 ++ ed2
<br/>val eresult = ed.map(term => (term,1)).reduceByKey(_ + _)
<br/>val eresults = eresult.sortBy(x => (x._1.split(" ")(0),x._1.split(" ")(1)))
<br/>eresults.saveAsTextFile("result2")
