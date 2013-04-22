<!DOCTYPE html> 

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html lang="en">

<jsp:include page="fragments/headTag.jsp"/>

<body>
<div class="container">
    <jsp:include page="fragments/bodyHeader.jsp"/>
    <div class="navbar" style="width: 601px;">
    <div class="navbar-inner">
        <ul class="nav">
	           <c:choose>
			    <c:when test="${vet}">
			      <li>
			          <spring:url value="/vets/{vetId}" var="vetsUrl">
			              <spring:param name="vetId" value="${person.id}"/>
			          </spring:url>
			          <a href="${fn:escapeXml(vetsUrl)}">Personal information</a>
			      </li>
			      <li><a href="<spring:url value="/owners/find.html" htmlEscape="true" />"><i
                    class="icon-search"></i> Find owners</a></li>
			    </c:when>
			    <c:when test="${owner}">
			      <p>&nbsp;</p>
			      <li>          
			      	  <spring:url value="/owners/{ownerId}" var="ownerUrl">
			              <spring:param name="ownerId" value="${person.id}"/>
			          </spring:url>
			          <a href="${fn:escapeXml(ownerUrl)}">Personal information </a>
			      </li>
			    </c:when>
			  </c:choose>
			<li style="width: 140px;"><a href="<spring:url value="/vets.html" htmlEscape="true" />"><i
                    class="icon-th-list"></i>All veterinarians</a></li>
            <li style="width: 90px;"><a href="<spring:url value="/oups.html" htmlEscape="true" />"
                                        title="trigger a RuntimeException to see how it is handled"><i
                    class="icon-warning-sign"></i> Error</a></li>
            <li style="width: 80px;"><a href="#" title="not available yet. Work in progress!!"><i
                    class=" icon-question-sign"></i> Help</a></li>
        </ul>
    </div>
</div>
	<h2><fmt:message key="welcome"/> ${person.firstName} ${person.lastName}</h2>

    <jsp:include page="fragments/footer.jsp"/>

</div>
</body>

</html>
