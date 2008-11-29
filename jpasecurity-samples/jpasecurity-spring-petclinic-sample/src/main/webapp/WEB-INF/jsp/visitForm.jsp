<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2><c:if test="${visit.new}">New </c:if>Visit:</h2>

<form:form modelAttribute="visit">
  <b>Pet:</b>
  <table width="333">
    <tr>
    <thead>
      <th>Name</th>
      <th>Vet</th>
      <th>Birth Date</th>
      <th>Type</th>
      <th>Owner</th>
    </thead>
    </tr>
    <tr>
      <td>${visit.pet.name}</td>
      <td>${visit.vet.firstName} ${visit.vet.lastName}</td>
      <td><fmt:formatDate value="${visit.pet.birthDate}" pattern="yyyy-MM-dd"/></td>
      <td>${visit.pet.type.name}</td>
      <td>${visit.pet.owner.firstName} ${visit.pet.owner.lastName}</td>
    </tr>
  </table>

  <table width="333">
    <tr>
      <th>
        Date:
        <br/><form:errors path="date" cssClass="errors"/>
      </th>
      <td>
        <c:choose>
          <c:when test="${pet.new}">
            <form:input path="date" size="10" maxlength="10"/> (yyyy-mm-dd)
          </c:when>
          <c:otherwise>
            <fmt:formatDate value="${visit.date}" pattern="yyyy-MM-dd"/>
          </c:otherwise>
        </c:choose>
      </td>
    <tr/>
    <tr>
      <th>
        Vet:
        <br/><form:errors path="vet" cssClass="errors"/>
      </th>
      <td>
        <c:choose>
          <c:when test="${pet.new}">
            <form:select path="vet" items="${vets}"/>
          </c:when>
          <c:otherwise>
            ${visit.vet.firstName} ${visit.vet.lastName}
          </c:otherwise>
        </c:choose>
      </td>
    </tr>
    <tr>
      <th valign="top">
        Description:
        <br/><form:errors path="description" cssClass="errors"/>
      </th>
      <td>
        <form:textarea path="description" rows="10" cols="25"/>
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <c:choose>
          <c:when test="${pet.new}">
            <input type="hidden" name="petId" value="${visit.pet.id}"/>
            <p class="submit"><input type="submit" value="Add Visit"/></p>
          </c:when>
          <c:otherwise>
            <p class="submit"><input type="submit" value="Update Visit"/></p>
          </c:otherwise>
        </c:choose>
      </td>
    </tr>
  </table>
</form:form>

<br/>
<b>Previous Visits:</b>
<table width="333">
  <tr>
    <th>Date</th>
    <th>Description</th>
  </tr>
  <c:forEach var="visit" items="${visit.pet.visits}">
    <c:if test="${!visit.new}">
      <tr>
        <td><fmt:formatDate value="${visit.date}" pattern="yyyy-MM-dd"/></td>
        <td>${visit.description}</td>
      </tr>
    </c:if>
  </c:forEach>
</table>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
