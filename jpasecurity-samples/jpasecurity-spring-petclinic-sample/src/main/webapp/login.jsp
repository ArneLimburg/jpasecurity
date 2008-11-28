<%@ page import="org.springframework.security.ui.AbstractProcessingFilter" %>
<%@ page import="org.springframework.security.ui.webapp.AuthenticationProcessingFilter" %>
<%@ page import="org.springframework.security.AuthenticationException" %>
<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

	<p>Valid users:
	<p>
	<p>username <b>james</b>, password <b>koala</b>
	<br>username <b>helen</b>, password <b>emu</b>
	<br>username <b>linda</b>, password <b>wombat</b>
	<br>username <b>raphael</b>, password <b>opal</b>
	<br>username <b>henry</b>, password <b>koala</b>
	<br>username <b>sharon</b>, password <b>emu</b>
	<br>username <b>george</b>, password <b>wombat</b>
	<br>username <b>betty</b>, password <b>opal</b>
	<br>username <b>rod</b>, password <b>koala</b>
	<br>username <b>harold</b>, password <b>emu</b>
	<br>username <b>jean</b>, password <b>wombat</b>
	<br>username <b>peter</b>, password <b>opal</b>
	<br>username <b>rod</b>, password <b>koala</b>
	<br>username <b>dianne</b>, password <b>emu</b>
	<br>username <b>scott</b>, password <b>wombat</b>
	<br>username <b>peter</b>, password <b>opal</b>
	<p>

    <form name="f" action="<c:url value='j_spring_security_check'/>" method="POST">
      <table>
        <tr><td>User:</td><td><input type='text' name='j_username' value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/></td></tr>
        <tr><td>Password:</td><td><input type='password' name='j_password'></td></tr>
        <tr><td><input type="checkbox" name="_spring_security_remember_me"></td><td>Don't ask for my password for two weeks</td></tr>

        <tr><td colspan='2'><p class="submit"><input name="submit" type="submit"></p></td></tr>
        <tr><td colspan='2'><p class="submit"><input name="reset" type="reset"></p></td></tr>
      </table>

    </form>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
