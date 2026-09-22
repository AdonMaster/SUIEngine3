package app.adon.suiengine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.adon.suiengine.renderer.resource.Resource
import suiengine.shared.generated.resources.Res

class AppVM: ViewModel() {

    val example = Resource(viewModelScope) {
        Res.readBytes("files/example01.js")
            .decodeToString()
    }

}