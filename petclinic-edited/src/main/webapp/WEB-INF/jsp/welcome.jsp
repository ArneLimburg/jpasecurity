<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<img src="<spring:url value="/static/images/pets.png" htmlEscape="true" />" align="right" style="position:relative;right:30px;">
<h2><fmt:message key="welcome"/> ${person.firstName} ${person.lastName}</h2>

<ul>
  <c:choose>
    <c:when test="${vet}">
      <li>
          <spring:url value="vets/{vetId}" var="vetsUrl">
              <spring:param name="vetId" value="${person.id}"/>
          </spring:url>
          <a href="${fn:escapeXml(vetsUrl)}">Personal information</a>
      </li>
      <li><a href="<spring:url value="/owners/search" htmlEscape="true" />">Find owner</a></li>
    </c:when>
    <c:when test="${owner}">
      <p>&nbsp;</p>
      <li>          
      	  <spring:url value="owners/{ownerId}" var="ownerUrl">
              <spring:param name="ownerId" value="${person.id}"/>
          </spring:url>
          <a href="${fn:escapeXml(ownerUrl)}">Personal information </a>
      </li>
    </c:when>
  </c:choose>
  <li><a href="<spring:url value="/vets" htmlEscape="true" />">All veterinarians</a></li>
</ul>

<p>&nbsp;</p>
<p>&nbsp;</p>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
