<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Search</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <link href="<c:url value="/resources/theme/css/style.css"/>" rel="stylesheet">
</head>
<body>
<header>
    <div class="container">
        <div id="branding">
            <h1><span class="highlight">Simin</span> Home Service</h1>
        </div>
        <nav>
            <ul>
                <li><a href="/">Home</a></li>
                <li><a href="/admin">Admin</a></li>
                <li class="current"><a href="/admin/manageExpert/expertSearch">Expert Search</a></li>
                <li><a href="/logout">Log Out</a></li>
            </ul>
        </nav>
    </div>
</header>
<center>
<form>
    <div class="w-75 t-3 s-3 e-3  align-items-center">
        <table class="table">
            <tbody>
            <tr>
                <th scope="row">Name</th>
                <td><input id="name" type="text" class="form-control form-control-sm" placeholder="Enter Name"></td>
                <th>Family</th>
                <td><input id="family" type="text" class="form-control form-control-sm" placeholder="Enter Family"></td>
                <th scope="row">Email</th>
                <td><input id="email" type="text" class="form-control form-control-sm" placeholder="Enter Email"></td>
            </tr>
            <tr>
                <th>Service</th>
                <td><input id="subService" type="text" class="form-control form-control-sm" placeholder="Enter Service"></td>
                <th scope="row">Score</th>
                <td><input id="score" type="text" class="form-control form-control-sm" placeholder="Enter Score"></td>
                <th>Confirmation State</th>
                <td>
                    <div class="form-group">
                        <select class="form-control  form-control-sm" id="sel1">
                            <option size="sm">--</option>
                            <option size="sm">CONFIRMED</option>
                            <option size="sm">WAITING_TO_BE_CONFIRMED</option>
                        </select>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</form>
<button id="search" class="btn btn-primary">Search</button>
<div class="w-75 p-3">
    <table id="tb" class="table">
        <thead>
        <th>Expert ID</th>
        <th>Name</th>
        <th>Family</th>
        <th>Email</th>
        <th>Confirmation State</th>
        <th>Image</th>
        <th>Score</th>
        <th>Select</th>
        </thead>

        <tbody>

        </tbody>
    </table>
</div>
<p id="myId"></p>

    <div  id="billDiv" class="w-75 p-3" style="display: none">
        <table id="billTB" class="table">
            <thead>
            <th>Expert Name</th>
            <th>Amount</th>
            <th>SubService Name</th>
            <th>Service Name</th>
            <th>Paying Status</th>
            <th>Issue Date</th>
            </thead>

            <tbody>

            </tbody>
        </table>

    </div>
</center>
</body>

<script>
    $("#search").click(function () {
        var msg = "";
        var table = document.getElementById("tb");
        for (var i = table.rows.length - 1; i > 0; i--) {
            table.deleteRow(i);
        }
        var email = $("#email").val();
        var name = $("#name").val();
        var family = $("#family").val();
        var subService = $("#subService").val();
        var score = $("#score").val();
        var confirmationState = null;
        if($("#sel1").val() != "--"){
            confirmationState = $("#sel1").val();
        }

        var arr = {name:name, family:family, email:email, confirmationState:confirmationState, score:score, subServicesList:[{name: subService}]};
        $.ajax({
            type: "POST",
            url: "http://localhost:8080/expert/search",
            data: JSON.stringify(arr),
            contentType: 'application/json; charset=utf-8',
            success: function (result) {
                $.each(result, function (index, value) {
                    msg += "<tr><td>" + value.id + "</td><td>" + value.name + "</td><td>" + value.family + "</td><td>" + value.email +
                        "</td><td>" + value.confirmationState + "</td><td>" + value.photo + "</td>" +
                        "<td>"+value.score+"</td>" +
                        "<td><input class=\"form-check-input radioB\" type=\"radio\" name=\"flexRadioDefault\" onchange=\"changeHandler()\"></td></tr>";
                });
                $(msg).appendTo("#tb tbody");
                //$("#myId").text(JSON.stringify(result));
            },
            error: function (result) {
                $("#myId").text(JSON.stringify(result));
            }
        });
    });


    /************change handler***********/
    var expertID;
    function changeHandler(){

        $("#billDiv").css("display", "block");
        var currentRow= $('input[name="flexRadioDefault"]:checked').closest('tr');
        expertID=currentRow.find("td:eq(0)").text();
        var table = document.getElementById("billTB");
        for (var i = table.rows.length - 1; i > 0; i--) {
            table.deleteRow(i);
        }
        var msg="";
        $.ajax({
            type: "GET",
            url: "http://localhost:8080/bill/getAllExpertBill/"+parseInt(expertID),
            success: function (result) {
                $.each(result, function (index, value) {
                    msg += "<tr><td>" + value.expert.name + "</td><td>" + value.amount +"</td><td>"+value.orders.subServices.name+
                        "</td><td>"+value.orders.subServices.services.name+
                        "</td><td>"+value.paymentStatus+"</td><td>" +value.issueDate+
                        "</td></tr>";
                });
                $(msg).appendTo("#billTB tbody");
            },
            error: function (result) {
                $("#myId").text(JSON.stringify(result));
            }
        });

    }
</script>

</html>
