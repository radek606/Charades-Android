package com.ick.kalambury.words

import com.ick.kalambury.wordsrepository.Id

class InstanceId(private vararg val params: Any): Id {

    override fun getId(): String {
        return params.joinToString("_")
    }

}