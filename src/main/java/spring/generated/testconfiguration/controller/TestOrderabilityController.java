package spring.generated.testconfiguration.controller;

import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import spring.generated.forms.TestOrderabilityForm;

import spring.mine.common.controller.BaseController;
import spring.mine.common.form.BaseForm;
import spring.mine.common.validator.BaseErrors;
import us.mn.state.health.lims.common.services.DisplayListService;
import us.mn.state.health.lims.common.services.TestService;
import us.mn.state.health.lims.common.services.TypeOfSampleService;
import us.mn.state.health.lims.common.util.IdValuePair;
import us.mn.state.health.lims.hibernate.HibernateUtil;
import us.mn.state.health.lims.test.beanItems.TestActivationBean;
import us.mn.state.health.lims.test.dao.TestDAO;
import us.mn.state.health.lims.test.daoimpl.TestDAOImpl;
import us.mn.state.health.lims.test.valueholder.Test;

@Controller
public class TestOrderabilityController extends BaseController {
  @RequestMapping(
      value = "/TestOrderability",
      method = RequestMethod.GET
  )
  public ModelAndView showTestOrderability(HttpServletRequest request,
      @ModelAttribute("form") TestOrderabilityForm form) {
    String forward = FWD_SUCCESS;
    if (form == null) {
    	form = new TestOrderabilityForm();
    }
        form.setFormAction("");
    BaseErrors errors = new BaseErrors();
    if (form.getErrors() != null) {
    	errors = (BaseErrors) form.getErrors();
    }
    ModelAndView mv = checkUserAndSetup(form, errors, request);

    if (errors.hasErrors()) {
    	return mv;
    }
    
    List<TestActivationBean> orderableTestList = createTestList(false);
    form.setOrderableTestList(orderableTestList);
    
    return findForward(forward, form);}
  
  private List<TestActivationBean> createTestList(boolean refresh) {
      ArrayList<TestActivationBean> testList = new ArrayList<TestActivationBean>();

      if (refresh) 
    	  DisplayListService.refreshList(DisplayListService.ListType.SAMPLE_TYPE_ACTIVE);
      	  
      List<IdValuePair> sampleTypeList = DisplayListService.getList(DisplayListService.ListType.SAMPLE_TYPE_ACTIVE);
      
      for( IdValuePair pair : sampleTypeList){
          TestActivationBean bean = new TestActivationBean();

          List<Test> tests = TypeOfSampleService.getAllTestsBySampleTypeId(pair.getId());
          List<IdValuePair> orderableTests = new ArrayList<IdValuePair>();
          List<IdValuePair> inorderableTests = new ArrayList<IdValuePair>();

          //initial ordering will be by display order.  Inactive tests will then be re-ordered alphabetically
          Collections.sort(tests, new Comparator<Test>() {
              @Override
              public int compare(Test o1, Test o2) {
              	//compare sort order
              	if (NumberUtils.isNumber(o1.getSortOrder()) && NumberUtils.isNumber(o2.getSortOrder())) {
              		return Integer.parseInt(o1.getSortOrder()) - Integer.parseInt(o2.getSortOrder());
                  //if o2 has no sort order o1 does, o2 is assumed to be higher
              	} else if (NumberUtils.isNumber(o1.getSortOrder())){
                  	return -1;
                  //if o1 has no sort order o2 does, o1 is assumed to be higher
                  } else if (NumberUtils.isNumber(o2.getSortOrder())) {
                  	return 1;
                  //else they are considered equal
                  } else {
                  	return 0;
                  }
              }
          });

          for( Test test : tests) {
              if( test.getOrderable()) {
                  orderableTests.add(new IdValuePair(test.getId(), TestService.getUserLocalizedTestName(test)));
              }else{
                  inorderableTests.add(new IdValuePair(test.getId(), TestService.getUserLocalizedTestName(test)));
              }
          }

          IdValuePair.sortByValue( orderableTests);

          bean.setActiveTests(orderableTests);
          bean.setInactiveTests(inorderableTests);
          if( !orderableTests.isEmpty() || !inorderableTests.isEmpty()) {
              bean.setSampleType(pair);
              testList.add(bean);
          }
      }

      return testList;
  }

  protected ModelAndView findLocalForward(String forward, BaseForm form) {
    if ("success".equals(forward)) {
      return new ModelAndView("testOrderabilityDefinition", "form", form);
    } else {
      return new ModelAndView("PageNotFound");
    }
  }
  
  @RequestMapping(
	      value = "/TestOrderability",
	      method = RequestMethod.POST
	  )
	  public ModelAndView postTestOrderability(HttpServletRequest request,
	      @ModelAttribute("form") TestOrderabilityForm form) throws Exception {
	  
	    String forward = FWD_SUCCESS;

	    BaseErrors errors = new BaseErrors();
	    if (form.getErrors() != null) {
	    	errors = (BaseErrors) form.getErrors();
	    }
	    ModelAndView mv = checkUserAndSetup(form, errors, request);

	    if (errors.hasErrors()) {
	    	return mv;
	    }

	    String changeList = form.getJsonChangeList();
        
	    JSONParser parser=new JSONParser();

        JSONObject obj = (JSONObject)parser.parse(changeList);
        
        List<String> orderableTestIds = getIdsForActions("activateTest", obj, parser);
        List<String> unorderableTestIds = getIdsForActions("deactivateTest", obj, parser);

        List<Test> tests = getTests(unorderableTestIds, false);
        tests.addAll(getTests(orderableTestIds, true));

        Transaction tx = HibernateUtil.getSession().beginTransaction();

        TestDAO testDAO = new TestDAOImpl();

        try{
            for(Test test : tests){
                testDAO.updateData(test);
            }

            tx.commit();
        }catch( HibernateException e ){
            tx.rollback();
        }finally{
            HibernateUtil.closeSession();
        }

        TypeOfSampleService.clearCache();

	    List<TestActivationBean> orderableTestList = createTestList(true);
	    form.setOrderableTestList(orderableTestList);
	  
	  return findForward(forward, form);
  }
  
  private List<Test> getTests(List<String> testIds, boolean orderable) {
      List<Test> tests = new ArrayList<Test>();

      for( String testId : testIds){
          Test test = new TestService(testId).getTest();
          test.setOrderable( orderable );
          test.setSysUserId(currentUserId);
          tests.add(test);
      }

      return tests;
  }

  private List<String> getIdsForActions(String key, JSONObject root, JSONParser parser){
      List<String> list = new ArrayList<String>();

      String action = (String)root.get(key);

      try {
          JSONArray actionArray = (JSONArray)parser.parse(action);

          for(int i = 0 ; i < actionArray.size(); i++   ){
              list.add((String) ((JSONObject) actionArray.get(i)).get("id"));
          }
      } catch (ParseException e) {
          e.printStackTrace();
      }

      return list;
  }

  protected String getPageTitleKey() {
    return null;
  }

  protected String getPageSubtitleKey() {
    return null;
  }
}
