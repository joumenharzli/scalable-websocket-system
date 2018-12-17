import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import play.filters.gzip.GzipFilter

/**
 * Filters
 *
 * @author jaharzli
 */
class Filters @Inject()(gzipFilter: GzipFilter) extends DefaultHttpFilters(gzipFilter)
