package com.kivi.framework.crypto.sm2;

import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class Sm2PrivateKeyImpl extends BCECPrivateKey implements Sm2PrivateKey {

    /**
     * 
     */
    private static final long      serialVersionUID = 1L;
    private ECPrivateKeyParameters ecPrivateKeyParameters;
    private byte[]                 withId;

    public Sm2PrivateKeyImpl( BCECPrivateKey privateKey ) {
        super(Sm2KeyPairImpl.ALGO_NAME_EC, privateKey);
        this.ecPrivateKeyParameters = new ECPrivateKeyParameters(this.getD(), Sm2KeyPairImpl.DOMAIN_PARAMS);
    }

    public Sm2PrivateKeyImpl( String algoNameEc, ECPrivateKeyParameters priKey, Sm2PublicKeyImpl publicKey,
                              ECParameterSpec spec,
                              ProviderConfiguration configuration ) {
        super(algoNameEc, priKey, publicKey, spec, configuration);
        this.ecPrivateKeyParameters = priKey;
    }

    @Override
    public ECPrivateKeyParameters getPrivateKeyParameters() {
        return ecPrivateKeyParameters;
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
