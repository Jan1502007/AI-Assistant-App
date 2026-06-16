<App>
  <!-- LEFT PANEL: brand identity -->
  <div class="brand-panel">
    <GridTexture />         <!-- subtle orange dot grid -->
    <GlowBlob />            <!-- radial orange ambient glow -->
    <Logo />                <!-- AXIOM wordmark + diamond -->
    <Headline>
      Sign in to
      <span class="orange">your space.</span>
    </Headline>
    <FeatureList>
      <Item>End-to-end encrypted sessions</Item>
      <Item>Multi-factor authentication ready</Item>
      <Item>Single sign-on via Google OAuth 2.0</Item>
    </FeatureList>
    <Footer>© 2026 AXIOM SYSTEMS</Footer>
  </div>

  <!-- RIGHT PANEL: authentication form -->
  <div class="auth-panel">
    <Card style="dark, orange-bordered">

      <!-- Google OAuth — primary CTA -->
      <GoogleOAuthButton onClick={handleGoogleLogin}>
        <GoogleIcon />
        Continue with Google
      </GoogleOAuthButton>

      <Divider label="OR" />

      <!-- Email/Password fallback -->
      <InputField label="EMAIL ADDRESS" type="email" />
      <InputField label="PASSWORD" type="password" showToggle />

      <!-- Sign in submit -->
      <SubmitButton style="orange-gradient, chakra-petch uppercase">
        Sign in
      </SubmitButton>

      <SignUpLink>No account? Create one</SignUpLink>
    </Card>

    <LegalNote>Terms &amp; Privacy Policy</LegalNote>
  </div>
</App>
