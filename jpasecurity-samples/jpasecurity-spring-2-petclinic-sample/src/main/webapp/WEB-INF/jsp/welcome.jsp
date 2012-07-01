<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<img src="images/pets.png" align="right" style="position:relative;right:30px;">
<h2><fmt:message key="welcome"/> ${person.firstName} ${person.lastName}</h2>

<p>&nbsp;</p>

<ul>
  <c:choose>
    <c:when test="${vet}">
      <li><a href="<c:url value="/vet.do?vetId=${person.id}"/>">Personal information</a></li>
      <li><a href="<c:url value="/findOwners.do"/>">Find owner</a></li>
    </c:when>
    <c:when test="${owner}">
      <p>&nbsp;</p>
      <li><a href="<c:url value="/owner.do?ownerId=${person.id}"/>">Personal information</a></li>
    </c:when>
  </c:choose>
  <li><a href="<c:url value="/vets.do"/>">All veterinarians</a></li>
</ul>

<p>&nbsp;</p>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
