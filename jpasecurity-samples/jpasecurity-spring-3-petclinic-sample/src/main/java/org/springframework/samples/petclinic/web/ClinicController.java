
package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Credential;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Vets;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Annotation-driven <em>MultiActionController</em> that handles all non-form
 * URL's.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
public class ClinicController {

	private final Clinic clinic;


	@Autowired
	public ClinicController(Clinic clinic) {
		this.clinic = clinic;
	}

	/**
	 * Custom handler for the welcome view.
	 * <p>
	 * Note that this handler relies on the RequestToViewNameTranslator to
	 * determine the logical view name based on the request URL: "/welcome.do"
	 * -&gt; "welcome".
	 */
	@RequestMapping("/login")
    public String loginHandler() {
        return "login";
    }

	/**
     * Custom handler for the welcome view.
     * <p>
     * Note that this handler relies on the RequestToViewNameTranslator to
     * determine the logical view name based on the request URL: "/welcome.do"
     * -&gt; "welcome".
     */
    @RequestMapping("/")
    public ModelAndView welcomeHandler() {
        Credential credential = (Credential)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelAndView mav = new ModelAndView("welcome");
        mav.addObject("person", credential.getUser());
        mav.addObject("vet", credential.getUser() instanceof Vet);
        mav.addObject("owner", credential.getUser() instanceof Owner);
        return mav;
    }

	/**
	 * Custom handler for displaying vets.
	 *
	 * <p>Note that this handler returns a plain {@link ModelMap} object instead of
	 * a ModelAndView, thus leveraging convention-based model attribute names.
	 * It relies on the RequestToViewNameTranslator to determine the logical
	 * view name based on the request URL: "/vets.do" -&gt; "vets".
	 *
	 * @return a ModelMap with the model attributes for the view
	 */
	@RequestMapping("/vets")
	public ModelMap vetsHandler() {
		Vets vets = new Vets();
		vets.getVetList().addAll(this.clinic.getVets());
		return new ModelMap(vets);
	}

	/**
	 * Custom handler for displaying an owner.
	 *
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@RequestMapping("/owners/{ownerId}")
	public ModelAndView ownerHandler(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/show");
		mav.addObject(this.clinic.loadOwner(ownerId));
		return mav;
	}

	/**
	 * Custom handler for displaying an list of visits.
	 *
	 * @param petId the ID of the pet whose visits to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@RequestMapping(value="/owners/*/pets/{petId}/visits", method=RequestMethod.GET)
	public ModelAndView visitsHandler(@PathVariable int petId) {
		ModelAndView mav = new ModelAndView("visits");
		mav.addObject("visits", this.clinic.loadPet(petId).getVisits());
		return mav;
	}

	/**
     * Custom handler for displaying a vet.
     *
     * @param vetId the ID of the vet to display
     * @return a ModelMap with the model attributes for the view
     */
    @RequestMapping(value="/vets/{vetId}")
    public ModelAndView vetHandler(@PathVariable("vetId") int vetId) {
        ModelAndView mav = new ModelAndView("vets/vet");
        Vet vet = this.clinic.loadVet(vetId);
        mav.addObject(vet);
        mav.addObject("visits", this.clinic.findVisits(vet));
        return mav;
    }
}
