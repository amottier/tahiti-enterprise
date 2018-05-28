package com.company.rest.api;

import groovy.json.JsonSlurper

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import spock.lang.Specification

import com.bonitasoft.engine.api.APIClient;
import com.bonitasoft.engine.api.IdentityAPI;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.company.model.VacationAvailable;
import com.company.model.VacationAvailableDAO;
import com.company.model.VacationRequest;
import com.company.model.VacationRequestDAO;

/**
 * @see http://spockframework.github.io/spock/docs/1.0/index.html
 */
class IndexTest extends Specification {

	// Declare mocks here
	// Mocks are used to simulate external dependencies behavior
	def httpRequest = Mock(HttpServletRequest)
	def context = Mock(RestAPIContext)
	def apiSession = Mock(APISession)
	def apiClient = Mock(APIClient)
	def identityAPI = Mock(IdentityAPI)
	def processAPI = Mock(ProcessAPI)
	def vacationRequestDAO = Mock(VacationRequestDAO)
	def vacationAvailableDAO = Mock(VacationAvailableDAO)
	def searchUsers = Mock(SearchResult)
	def william = Mock(User)
	def williamVacationAvailable = Mock(VacationAvailable)
	def april = Mock(User)
	def aprilVacationAvailable = Mock(VacationAvailable)
	def williamVacationRequest = Mock(VacationRequest)
	def williamVacationRequests = [williamVacationRequest]

	List<User> users = new ArrayList<>()

	/**
	 * You can configure mocks before each tests in the setup method
	 */
	def setup(){
	}

	def should_return_a_json_representation_as_result() {
		given: "a RestAPIController"
		def index = new Index()
		context.apiSession >> apiSession
		context.apiClient >> apiClient
		//context.createBusinessObjectDAO(VacationRequestDAO.class) >> vacationRequestDAO
		//context.createBusinessObjectDAO(VacationAvailableDAO.class) >> vacationAvailableDAO
		//context.createBusinessObjectDAO(_) >>> [vacationRequestDAO, vacationAvailableDAO]
		apiClient.getDAO(_) >>> [vacationRequestDAO, vacationAvailableDAO]
		apiClient.identityAPI >> identityAPI
		apiClient.processAPI >> processAPI
		identityAPI.searchUsers(_) >> searchUsers
		searchUsers.result >> [william, april]
		william.id >> 1
		william.firstName >> "William"
		william.lastName >> "Jobs"
		april.id >> 2
		april.firstName >> "April"
		april.lastName >> "Sabchez"
		vacationAvailableDAO.findByBonitaBPMId(1) >> williamVacationAvailable
		vacationAvailableDAO.findByBonitaBPMId(2) >> aprilVacationAvailable
		williamVacationAvailable.daysAvailableCounter >> 3
		aprilVacationAvailable.daysAvailableCounter >> 6
		vacationRequestDAO.findByRequesterBonitaBPMId(1, 0, 100) >> williamVacationRequests

		when: "Invoking the REST API"
		def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

		then: "A JSON representation is returned in response body"
		def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
		// Validate returned response
		apiResponse.httpStatus == 200
		jsonResponse.isManager == true
		jsonResponse.employeesVacationAvailable.size == 2
		jsonResponse.employeesVacationAvailable[0].daysAvailableCounter == 3
		jsonResponse.employeesVacationAvailable[0].firstName == "William"
		jsonResponse.employeesVacationAvailable[0].lastName == "Jobs"
		jsonResponse.employeesVacationRequests.size == 1

	}

}