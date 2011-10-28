package net.sf.jpasecurity.samples.elearning.jsf.view;

import static org.junit.Assert.assertEquals;

import net.sf.jpasecurity.samples.elearning.jsf.view.AbstractHtmlTestCase.Role;

import org.jaxen.JaxenException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ElearningAssert {

    public static void assertIndexPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        if(role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//a[@href = 'courses.xhtml'][text() = 'Courses']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'teachers.xhtml'][text() = 'Teachers']").size());
    }
    
    public static void assertCoursesPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Available courses']").size());     
        if(role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=1'][text() = 'Shakespeare course']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=2'][text() = 'Da Vinci course']").size());
    }

    public static void assertCoursePage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        switch (role) {
        case TEACHER:
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
            assertEquals(1, page.getByXPath("//a[@href = 'lessonCreater.xhtml'][text() = 'Create new Lesson']").size());
            assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'join this course']").size());
            assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'leave this course']").size());
        case STUDENT:
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
            assertEquals(0, page.getByXPath("//a[@href = 'lessonCreater.xhtml'][text() = 'Create new Lesson']").size());
            assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'join this course']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'leave this course']").size());
        case GUEST:
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
            assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'join this course']").size());
            assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'leave this course']").size());
        }
        assertEquals(1, page.getByXPath("//h1[text() = 'Shakespeare course']").size());
        assertEquals(1, page.getByXPath("//h2[text() = 'Lecturer']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'teacher.xhtml?id=1'][text() = 'Peter B.']").size());
        assertEquals(1, page.getByXPath("//h2[text() = 'Participants']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=2'][text() = 'Stefan A.']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=4'][text() = 'Tassimo B.']").size());
        assertEquals(0, page.getByXPath("//a[@href = 'student.xhtml?id=5'][text() = 'Ulli D.']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=6'][text() = 'Anne G.']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'student.xhtml?id=7'][text() = 'Lisa T.']").size());
        assertEquals(0, page.getByXPath("//a[@href = 'student.xhtml?id=8'][text() = 'Marie M.']").size());
        assertEquals(1, page.getByXPath("//h2[text() = 'Lessons']").size());
    }

    public static void assertDashboardPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        switch (role) {
        case TEACHER:
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
            assertEquals(1, page.getByXPath("//h1[text() = 'Create new course']").size());
            assertEquals(1, page.getByXPath("//h1[text() = 'My courses']").size());
            assertEquals(0, page.getByXPath("//h1[text() = 'Available courses']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'create']").size());
            assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=1'][text() = 'Shakespeare course']").size());
            assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?id=2'][text() = 'Da Vinci course']").size());
            assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=3'][text() = 'Analysis']").size());
            assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?id=4'][text() = 'Algebra']").size());
        case STUDENT:
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
            assertEquals(0, page.getByXPath("//h1[text() = 'Create new course']").size());
            assertEquals(1, page.getByXPath("//h1[text() = 'My courses']").size());
            assertEquals(1, page.getByXPath("//h1[text() = 'Available courses']").size());
            assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'create']").size());
            assertEquals(2, page.getByXPath("//a[@href = 'course.xhtml?id=1'][text() = 'Shakespeare course']").size());
            assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=2'][text() = 'Da Vinci course']").size());
            assertEquals(2, page.getByXPath("//a[@href = 'course.xhtml?id=3'][text() = 'Analysis']").size());
            assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=4'][text() = 'Algebra']").size());
        case GUEST:
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Username:']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Password:']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Login']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'reset'][@value = 'Cancel']").size());
        }
    }

    public static void assertLessonCreaterPage(HtmlPage page,Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Create new lesson']").size());        
        if (role == Role.TEACHER) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Course name:']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Lesson name:']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Text:']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'cancel']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'create new lesson']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
    }

    public static void assertLessonPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        if(role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//h2[text() = 'Content']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Course:']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Lecturer:']").size());
        assertEquals(0, page.getByXPath("//input[@type = 'submit'][@value = 'resolve this lesson']").size());
        assertEquals(0, page.getByXPath("//div[text() = 'Lesson is resolved']").size());
    }

    public static void assertLoginPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        if(role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//label[text() = 'Username:']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Password:']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Login']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Cancel']").size());
    }

    public static void assertStudentPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Marie M.']").size());
        if(role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//h2[text() = 'Selected Courses']").size());
        assertEquals(0, page.getByXPath("//a[@href = 'course.xhtml?id=1'][text() = 'Shakespeare course']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=2'][text() = 'Da Vinci course']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=3'][text() = 'Analysis']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=4'][text() = 'Algebra']").size());
    }

    public static void assertTeacherPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Peter B.']").size());
        if(role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//h2[text() = 'Lectured Courses']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=1'][text() = 'Shakespeare course']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=3'][text() = 'Analysis']").size());
    }

    public static void assertTeachersPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Teachers']").size());      
        if(role == Role.TEACHER || role == Role.STUDENT) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//a[@href = 'teacher.xhtml?id=1'][text() = 'Peter B.']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'teacher.xhtml?id=3'][text() = 'Hans L.']").size());
    }
}
