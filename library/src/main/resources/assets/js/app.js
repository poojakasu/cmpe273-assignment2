$(document).ready(function(){
if(window.WebSocket) {
	Validate();
	
	  var admin = "admin";
	  var password = "password";
	  if($("#libHeader").html() == "Library:A")
		  var destination = "/topic/05829.book.*";
	  else if($("#libHeader").html() == "Library:B")
		  var destination = "/topic/05829.book.computer";
	  else
		  var destination = "/topic/05829.book.*";
	  var client = Stomp.client("ws://54.219.156.168:61623");

	  client.debug = function(str) {
          $("#message").html("Status :"+ str);
        };
        
	  client.connect(admin,password,
			  function(){
		  				  client.debug("connected to Stomp");		  				  
						  client.subscribe(destination,
								  function(fram){
							  		$("#message").html("Receieved :"+ fram.body);
							  		var temp = fram.body;
							  		var arrtemp = temp.split(':');
							  		$.ajax({
									    type:"GET",
									    url: "v1/books/"+arrtemp[0],
									    async:false,
									    error:function(){
									    	var obj='{'+'\"isbn\":\"'+arrtemp[0]+'\",\"title\":\"'+arrtemp[1]+'\",\"category\":\"'+arrtemp[2]+'\",\"coverimage\":\"'+arrtemp[3]+':'+arrtemp[4]+'\"}';
									    	try{
									    	try{
									    		var jsonobj = $.parseJSON(obj);
									    		var jsonobj = JSON.stringify(jsonobj);
									    	}
									    	catch(e)
									    	{
									    		alert(e);
									    	}
								    		$.ajax({
							    			    type:"POST",
							    			    url: "library/v1/books",
							    			    contentType:'application/json',
							    			    async:false,
							    			    data:jsonobj,
							    			    error:function(){alert("Error")},
							    			    complete:RefreshPage(obj)
							    			    });
									    	}
									    	catch(e){
									    		
									    	}
									    },
									    success: function(xhr){
									    	var tempStatus = "#"+arrtemp[0]+"_status";
									    	$(tempStatus).html("avaliable");
									    	var tempButton = "#"+arrtemp[0];
									    	$(tempButton).removeAttr("disabled");
								    		$.ajax({
							    			    type:"PUT",
							    			    url: "library/v1/books/"+arrtemp[0]+"?status=available",
							    			    async:false
							    			    });
									    		
									    }
									    });	
						  			
						  				   });
	  					}
	  				
	  				 );//this is end of client.connect
  
}
else
	{
	$("#connect").html("\
            <h1>Get a new Web Browser!</h1>\
            <p>\
            Your browser does not support WebSockets. This example will not work properly.<br>\
            Please use a Web Browser with WebSockets support (WebKit or Google Chrome).\
            </p>\
        ");
	}
});

$(":button").click(function() {		
	var isbn = this.id;
    alert('About to report lost on ISBN ' + isbn);
    $.ajax({
	    type:"PUT",
	    url: "library/v1/books/"+isbn+"?status=lost",
	    async:false,
	    complete: Load_View(isbn)
	    });	 
});

function btn_clicked(obj)
{
	var isbn = obj.id;
	 alert('About to report lost on ISBN ' + isbn);
	 $.ajax({
		    type:"PUT",
		    url: "library/v1/books/"+isbn+"?status=lost",
		    async:false,
		    complete: Load_View(isbn)
		    });	 
}

function RefreshPage(obj){	
	test = JSON.stringify(obj);
	arrTest = test.split(":");
	if(arrTest.length > 5){
	var view = {
	        title : test.split(":")[2].split(",")[0].replace(/\"/g,"").replace(/\\/g,""),
	        isbn : test.split(":")[1].split(",")[0].replace(/\"/g,"").replace(/\\/g,""),
	        category : test.split(":")[3].split(",")[0].replace(/\"/g,"").replace(/\\/g,""),
	        status : "available",
	        coverimage : test.split(":")[4].replace(/\"/g,"").replace(/\\/g,"")+":"+test.split(":")[5].replace(/\"/g,"").replace(/\\/g,"").replace("}","")
	      };
		
	var template = "<tr> \
	<td>{{isbn}}</td> \
	<td><img class=\"img-thumbnail\" height=\"92\" width=\"72\" src=\"{{coverimage}}\"></td> \
	<td>{{title}}</td> \
	<td>{{category}}</td> \
	<td><p id=\"{{isbn}}_status\">{{status}}</p></td> \
	<td><button id=\"{{isbn}}\" type=\"button\" class=\"btn btn-primary\" onclick =\"btn_clicked(this)\">Report Lost</button></td> \
	</tr>"
		var article1 = Mustache.to_html(template, view);
		$("#tblbook").append(article1);
	}
}

function Load_View(isbn){
	var tempStatus = "#"+isbn+"_status";
	var tempButton = "#"+isbn;
	$(tempStatus).html("lost");
	$(tempButton).attr("disabled", "disabled");
}

function Validate(){
	var txt = "Library:";
	var libInstance = $("#libHeader").html();
	if(libInstance.length> 0 && libInstance.split("-")[1] == "a")
		txt += "A";
	else
		txt += "B";
	$("#libHeader").html(txt);		
	var lststatus = $("p");
	for(var i=0;i<lststatus.length;i++){
		if($(lststatus[i]).html() == "lost"){
			var temp = lststatus[i].id;
			temp = temp.split('_')[0];
			var tempButton = "#"+temp;
			$(tempButton).attr("disabled", "disabled");
		}
			
	}
}

