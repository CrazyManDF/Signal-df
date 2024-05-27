package org.thoughtcrime.securesms.components

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import org.signal.core.util.logging.Log
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewBinderDelegate<T : ViewBinding>(
    private val bindingFactory: (View) -> T,
    private val onBindingWillBeDestroyed: (T) -> Unit = {}
) : DefaultLifecycleObserver, ReadOnlyProperty<Fragment, T>{

    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (binding == null){
            if (!thisRef.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)){
                error("Invalid state to create a binding.")
            }
            thisRef.viewLifecycleOwner.lifecycle.addObserver(this@ViewBinderDelegate)
            binding = bindingFactory(thisRef.requireView())
        }
        Log.d(">>>", "getValue===========")
        return  binding!!
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        if (binding != null) {
            onBindingWillBeDestroyed(binding!!)
        }
        binding = null
    }
}