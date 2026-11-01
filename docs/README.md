# UniEnable

UniEnable is a Java command-line application for managing dated activities and
checking recorded campus accessibility information.

Useful links:

* [User Guide](UserGuide.md)
* [Developer Guide](DeveloperGuide.md)
* [About Us](AboutUs.md)
* [Team Project Portfolios](team/README.md)

## Publication and link checks

For a GitHub Pages deployment from this repository, select the `master` branch
and `/docs` folder as the publishing source in the repository's Pages settings,
or use the team's configured Pages workflow. The published guides are available
at the [team Pages site](https://ay2627s1-cs2113-t10-4.github.io/tp/). Deploy the
reviewed documentation revision before checking the public pages.

The Developer Guide links Java files, resources, and root-level files to the
repository's `master` branch on GitHub so they also work from published pages.
Diagram navigation uses the [source index](diagrams/README.md)
and [image index](images/README.md). Luke contributes the activity-creation class
diagram, name/parameter validation pairs, and architecture diagram. Atharva
contributes the add sequence, calendar, help, schedule validation, and date
parsing diagrams.

After deployment, open the User Guide, Developer Guide, About Us, portfolios,
diagram index, and image index from the published home page. Follow a source-code
link, a dataset link, an index link, and an image link from the Developer Guide;
check diagram legibility at normal desktop and narrow viewport widths. Diagrams
retain their original text size: when an image is wider than the page, verify
that its region scrolls horizontally, including after focusing it with Tab and
pressing an arrow key. Check that each enlarge link still opens the full image.

Pages deployment requires GitHub Actions to be enabled for the team repository.

### Verify the published revision

Record the reviewed commit with `git rev-parse HEAD`. In the team repository's
Pages deployment details, check that the successful deployment corresponds to
the intended publishing commit. A successful Java CI run checks the application;
it does not establish that the documentation website has been updated.

Open the [published facility and route tests](https://ay2627s1-cs2113-t10-4.github.io/tp/DeveloperGuide.html#facility-queries-and-routes).
Confirm that the rejected-input table is present and says the sample-data notice
is absent for invalid route inputs. Compare it with the
[reviewed Markdown](DeveloperGuide.md#facility-queries-and-routes) and try its
commands using the released JAR in a separate test folder. If the page still
shows the older blanket notice requirement, check the publishing branch/folder,
deployment result and browser cache before treating the application as faulty.

Merging into the configured publishing source, or triggering its configured
deployment workflow, is required to update the public site. A local commit on a
fix branch does not publish the correction. GitHub documents this behavior in
[its Pages publishing guidance](https://docs.github.com/en/pages/setting-up-a-github-pages-site-with-jekyll/adding-content-to-your-github-pages-site-using-jekyll).
