package gonorth

import kategory.Option

/**
 * Utility method to turn a kotlin Nullable into an option.
 * This will eventually be in kategory, at which point this
 * method can be deleted.
 */
fun <T> T?.toOpt(): Option<T> = Option.fromNullable(this)
