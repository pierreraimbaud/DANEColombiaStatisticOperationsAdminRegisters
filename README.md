# DANE - Colombian Statistics - Making new policies from metadata
### Authors
Pierre Raimbaud : https://github.com/pierreraimbaud<br/>
Camilo Espitia: https://github.com/camilospn<br/>
GitHub project link : https://github.com/pierreraimbaud/DANEColombiaStatisticOperationsAdminRegisters<br/>
Projet page : https://pierreraimbaud.github.io/DANEColombiaStatisticOperationsAdminRegisters/demo<br/><br/>
<b>Paper</b>: https://github.com/pierreraimbaud/DANEColombiaStatisticOperationsAdminRegisters/blob/master/IEEE%20VIS%20Camilo%20y%20Pierre%20-%20DANE%20-%20Data%20driven%20Policy%20Making%20in%20Colombia.pdf<br/><br/>
This project is under MIT license (applied to the repository).<br/><br/>
<h3>Example of visualization of our tool</h3><br/>
<img src="/registersAndOperationsAboutOneTheme.png" alt="registersAndOperationsAboutOneTheme"><br/><br/>
This visualization uses data from the Colombian public entity DANE. The principal objective here is to show or discover some interesting insights about this data, knowing that the principal question here is:<br/><b>Could I take better decisions for making new policies using current data from DANE ? And using the metadata present in these initial data? </b><br/>
In other terms, the objective is to build a visualization that allows :<br/>
- to provide a tool for the politics maker and for the public in general, providing an added-value for making decisions. <br/>
- to discover new taxonomies about Administrative Registers and Statistical Operations from Keywords.<br/><br/>
The other objetives are more academic: to use d3, to represent a network in a visualization, to publish the web page on GithubPages etc. The technologies used are d3 (javascript), HTML, CSS, Java (for processing the csv file into a json file) and git (nodejs for developing with a local server). There is no specific prerequisites for enjoying the visualization neither for using the code, available in github.
<br/><br/>
To be more precise, in the following paragraphs first we will explain (briefly!) what were our data (through data abstraction), why this visualization (through task abstraction) and to conclude the reason of how we choice to present the data (idioms : visual encoding and interaction) ; thanks to that, we have been able to answer to the question written above. Our answers will be the insights of this visualization!
<br/><br/>
<h2>WHAT</h2>
Inventory of Administrative Registers and Statistical Operations of the country (Table)<br/>
Keywords present in the previous inventory (in the metadata) and the Administrative Registers and Statistical Operations linked to them (Derived table)
<br/><br/>
<h2>WHY</h2>
Part 1<br/>
Summarize / Distribution<br/>
Part 2<br/>
Summarize / Distribution   -   Identify Extremes<br/>
Part 3<br/>
Identify / Features   -   Identify Extremes
<br/><br/>
<h2>HOW</h2>
Part 1:
<br/>
<img src="/vizPart1.png" alt="vizPart1"><br/>
Part 2:
<br/>
<img src="/vizPart2.PNG" alt="vizPart2"><br/>
Part 3:
<br/>
<img src="/vizPart3.PNG" alt="vizPart3"><br/>
