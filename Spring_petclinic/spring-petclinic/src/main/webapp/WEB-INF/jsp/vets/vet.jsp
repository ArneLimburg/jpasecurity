<!DOCTYPE html> 

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<html lang="en">

<jsp:include page="../fragments/headTag.jsp"/>

<body>
<div class="container">
    <jsp:include page="../fragments/bodyHeader.jsp"/>

<h2>Vet Information</h2>

  <table>
    <tr>
      <th>Name</th>
      <td><b>${vet.firstName} ${vet.lastName}</b></td>
    </tr>
    <tr>
      <th>Specialities</th>
      <td>
	    <c:forEach var="specialty" items="${vet.specialties}">
          ${specialty.name}
        </c:forEach>
        <c:if test="${vet.nrOfSpecialties == 0}">none</c:if>
      </td>
    </tr>
  </table>

<h2>Visits</h2>

  <c:forEach var="visit" items="${visits}">
    <table width="94%">
      <tr>
        <th>Date</th>
        <td><joda:format value="${visit.date}" pattern="yyyy-MM-dd"/></td>
      </tr>
      <tr>
        <th>Pet</th>
        <td>${visit.pet.name}</td>
      </tr>
      <tr>
        <th>Type</th>
        <td>${visit.pet.type.name}</td>
      </tr>
      <tr>
        <th>Owner</th>
        <td>
          <spring:url value="/owners/{ownerId}" var="ownerUrl">
            <spring:param name="ownerId" value="${visit.pet.owner.id}"/>
          </spring:url>
          <a href="${fn:escapeXml(ownerUrl)}">${visit.pet.owner.firstName} ${visit.pet.owner.lastName}</a>
        </td>
      </tr>
      <tr>
        <th>Description</th>
        <td>${visit.description}</td>
      </tr>
    </table>
    <table class="table-buttons">
      <tr>
        <access:updating entity="vet">
	        <td>
	          <spring:url value="/pets/{petId}/visits/{visitId}/edit" var="visitUrl">
	            <spring:param name="petId" value="${visit.pet.id}"/>
	            <spring:param name="visitId" value="${visit.id}"/>
	          </spring:url>
	          <a href="${fn:escapeXml(visitUrl)}">Edit Visit</a>
	        </td>
        </access:updating>
      </tr>
    </table>
  </c:forEach>
  
    <jsp:include page="../fragments/footer.jsp"/>
</div>
</body>

</html>