package spring.service.test;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import spring.service.common.BaseObjectService;
import us.mn.state.health.lims.test.valueholder.Test;

public interface TestService extends BaseObjectService<Test> {

	void getData(Test test);

	void deleteData(List tests);

	void updateData(Test test);

	boolean insertData(Test test);

	Test getActiveTestById(Integer id);

	Test getTestByUserLocalizedName(String testName);

	Integer getTotalTestCount();

	List getNextTestRecord(String id);

	List<Test> getAllActiveTests(boolean onlyTestsFullySetup);

	List getTestsByTestSectionAndMethod(String filter, String filter2);

	List<Test> getTestsByTestSectionId(String id);

	List getPageOfTestsBySysUserId(int startingRecNo, int sysUserId);

	Integer getTotalSearchedTestCount(String searchString);

	Integer getAllSearchedTotalTestCount(HttpServletRequest request, String searchString);

	List<Test> getActiveTestByName(String testName);

	List getPreviousTestRecord(String id);

	List getTestsByTestSection(String filter);

	List getPageOfSearchedTests(int startingRecNo, String searchString);

	List getAllTestsBySysUserId(int sysUserId, boolean onlyTestsFullySetup);

	List getMethodsByTestSection(String filter);

	List<Test> getActiveTestsByLoinc(String loincCode);

	List<Test> getAllActiveOrderableTests();

	Test getTestByDescription(String description);

	List<Test> getTestsByLoincCode(String loincCode);

	List<Test> getAllOrderBy(String columnName);

	boolean isTestFullySetup(Test test);

	Test getTestById(Test test);

	Test getTestById(String testId);

	List getTestsByMethod(String filter);

	List getPageOfTests(int startingRecNo);

	List getTests(String filter, boolean onlyTestsFullySetup);

	List<Test> getAllTests(boolean onlyTestsFullySetup);

	Test getTestByName(Test test);

	Test getTestByName(String testName);

	Test getTestByGUID(String guid);

	Integer getTotalSearchedTestCountBySysUserId(int sysUserId, String searchString);

	Integer getNextAvailableSortOrderByTestSection(Test test);

	List<Test> getPageOfSearchedTestsBySysUserId(int startingRecNo, int sysUserId, String searchString);

	void localeChanged(String locale);
}