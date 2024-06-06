package org.whispersystems.signalservice.api.push

import okio.ByteString
import org.whispersystems.signalservice.api.push.ServiceId.ACI
import org.whispersystems.signalservice.api.push.ServiceId.PNI

class ServiceIds(
    val aci: ACI? = null,
    val pni: PNI? = null
) {

    private var aciByteString: ByteString? = null
    private var pniByteString: ByteString? = null

    fun requirePni(): PNI {
       return requireNotNull(pni)
    }

    fun matches(serviceId: ServiceId): Boolean {
        return serviceId == aci || (pni != null && serviceId == pni)
    }

    fun matches(serviceIdsBytes: ByteString): Boolean {
        if (aciByteString == null){
            aciByteString = aci?.toByteString()
        }

        if (pniByteString == null && pni != null) {
            pniByteString = pni.toByteString()
        }
        return serviceIdsBytes == aciByteString || serviceIdsBytes == pniByteString
    }
}