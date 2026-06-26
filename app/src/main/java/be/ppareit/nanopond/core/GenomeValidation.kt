package be.ppareit.nanopond.core

import be.ppareit.nanopond.utils.isHex

internal fun isValidGenomeHex(text: CharSequence): Boolean {
    return text.length == NanoPond.POND_DEPTH && text.isHex()
}
