package controllers.user;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import services.ArticleService;
import services.NewspaperService;
import services.UserService;
import controllers.AbstractController;
import domain.Article;
import domain.Newspaper;
import domain.User;

@Controller
@RequestMapping("/article/user")
public class ArticleUserController extends AbstractController {

	// Services -------------------------------------------------------------

	@Autowired
	private ArticleService articleService;
	
	// Supporting services --------------------------------------------------
	
	@Autowired
	private NewspaperService newspaperService;
	
	@Autowired
	private UserService userService;
	
	// Constructors ---------------------------------------------------------

	public ArticleUserController() {
		super();
	}

	// Creation ---------------------------------------------------------------

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public ModelAndView create() {
		ModelAndView res;
		Article article;

		article = this.articleService.create();
		res = this.createEditModelAndView(article);
		
		return res;
	}

	// Editing ---------------------------------------------------------------

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public ModelAndView edit(@RequestParam final int articleId) {
		ModelAndView result;
		Article article;
		User user;

		user = this.userService.findByPrincipal();
		article = this.articleService.findOne(articleId);
		result = this.createEditModelAndView(article);
		
		if (user.getArticles().contains(article)) {
			article = this.articleService.findOne(articleId);
			result = this.createEditModelAndView(article);
			result.addObject("article", article);
		} else {
			result = new ModelAndView("redirect:../../");
		}
		
		return result;
	}

	// Saving --------------------------------------------------------------

	@RequestMapping(value = "/edit", method = RequestMethod.POST, params = "save")
	public ModelAndView save(@Valid Article article, final BindingResult binding) {
		ModelAndView res;
		article = this.articleService.reconstruct(article, binding);
		if (binding.hasErrors()) {
//			res = new ModelAndView("redirect:edit.do?articleId=" + article.getId());
			res = this.createEditModelAndView(article, "article.params.error");
		} else {
			try {
				this.articleService.save(article);
				res = new ModelAndView("redirect:../list.do?newspaperId=" + article.getNewspaper().getId());
				
			} catch (final Throwable oops) {
				res = this.createEditModelAndView(article,
						"article.commit.error");
			}
		}
		return res;
	}

	// Ancillary methods --------------------------------------------------

	protected ModelAndView createEditModelAndView(final Article article) {
		ModelAndView result;

		result = this.createEditModelAndView(article, null);

		return result;
	}

	protected ModelAndView createEditModelAndView(final Article article,
			final String message) {
		ModelAndView result;
		final Collection<Boolean> draftmode = new ArrayList<>();
		Collection<Newspaper> newspaper;
		User user;
		
		user = userService.findByPrincipal();
		draftmode.add(false);
		draftmode.add(true);
		newspaper = newspaperService.findNewspapersByUser(user.getId());
		newspaper.removeAll(newspaperService.findNewspapersPublicated());

		result = new ModelAndView("article/user/edit");
		result.addObject("article", article);
		result.addObject("draftmode", draftmode);
		result.addObject("newspaper", newspaper);
		result.addObject("message", message);
		if(article.getId() == 0){
			result.addObject("requestURI", "article/user/edit.do");
		} else {
			result.addObject("requestURI", "article/user/edit.do?articleId=" + article.getId());
		}


		return result;
	}

}
