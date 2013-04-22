<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ page import="org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter" %>
<%@ page import="org.springframework.security.core.AuthenticationException" %>
<html lang="en">


<jsp:include page="fragments/headTag.jsp"/>

<jsp:include page="fragments/bodyHeader.jsp"/>

<body>
  <div id="main">
    <form name="f" action="<c:url value='j_spring_security_check'/>" method="POST">
      <table>
        <tr><td>User:</td><td><input type='text' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
        <tr><td>Password:</td><td><input type='password' name='j_password'></td></tr>
        <tr><td><input type="checkbox" name="_spring_security_remember_me"></td><td>Don't ask for my password for two weeks</td></tr>

        <tr><td colspan='2'><p class="submit"><input name="submit" type="submit"></p></td></tr>
        <tr><td colspan='2'><p class="submit"><input name="reset" type="reset"></p></td></tr>
      </table>

    </form>

  <table class="footer">
    <tr>
      <td><a href="<c:url value="/owners/new"/>">Register</a></td>
    </tr>
  </table>

  </div>
</body>

</html>