package com.kivi.framework.crypto.sm2;

import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class Sm2PublicKeyImpl extends BCECPublicKey implements Sm2PublicKey {

    /**
     * 
     */
    private static final long     serialVersionUID = 1L;
    private ECPublicKeyParameters ecPublicKeyParameters;
    private byte[]                withId;

    public Sm2PublicKeyImpl( BCECPublicKey publicKey ) {
        super(Sm2KeyPairImpl.ALGO_NAME_EC, publicKey);
        this.ecPublicKeyParameters = new ECPublicKeyParameters(this.getQ(), Sm2KeyPairImpl.DOMAIN_PARAMS);
    }

    public Sm2PublicKeyImpl( String algoNameEc, ECPublicKeyParameters pubKey, ECParameterSpec spec,
                             ProviderConfiguration configuration ) {
        super(algoNameEc, pubKey, spec, configuration);
        this.ecPublicKeyParameters = pubKey;
    }

    @Override
    public ECPublicKeyParameters getPublicKeyParameters() {
        return ecPublicKeyParameters;
    }

    @Override
    public byte[] getWithId() {
        return withId;
    }

    @Override
    public void setWithId( byte[] withId ) {
        this.withId = withId;
    }

}
