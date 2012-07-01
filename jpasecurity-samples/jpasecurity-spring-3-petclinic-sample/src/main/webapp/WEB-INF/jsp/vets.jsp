<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2>Veterinarians:</h2>

<table>
  <thead>
    <th>Name</th>
    <th>Specialties</th>
  </thead>
  <c:forEach var="vet" items="${vets.vetList}">
    <tr>
      <td>
        <spring:url value="vets/{vetId}" var="vetUrl">
          <spring:param name="vetId" value="${vet.id}"/>
        </spring:url>
        <a href="${fn:escapeXml(vetUrl)}">${vet.firstName} ${vet.lastName}</a>
      </td>
      <td>
	    <c:forEach var="specialty" items="${vet.specialties}">
          ${specialty.name}
        </c:forEach>
        <c:if test="${vet.nrOfSpecialties == 0}">none</c:if>
      </td>
    </tr>
  </c:forEach>
</table>
<table class="table-buttons">
  <tr>
    <td>
      <a href="<spring:url value="/vets.xml" htmlEscape="true" />">View as XML</a>
    </td>
  </tr>
</table>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
