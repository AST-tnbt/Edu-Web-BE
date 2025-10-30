import { useEffect, useMemo, useState, type SyntheticEvent } from 'react';
import { Alert, Container, Grid, Snackbar, Stack } from '@mui/material';
import { useAuth } from '../hooks/useAuth';
import ProfileCard from '../components/ProfileCard';
import ProfileEditForm from '../components/ProfileEditForm';
import * as userService from '../services/userService';

const ProfilePage = () => {
  const { user, accessToken } = useAuth();
  const [profile, setProfile] = useState<userService.UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    if (!accessToken || !user?.id) {
      setError(null);
      setProfile(null);
      setLoading(false);
      return () => {
        mounted = false;
      };
    }

    setLoading(true);
    setError(null);

    userService
      .getProfile(accessToken, user.id)
      .then((data) => {
        if (!mounted) return;
        setProfile(data);
      })
      .catch((err) => {
        if (!mounted) return;
        setError(err instanceof Error ? err.message : String(err));
      })
      .finally(() => {
        if (!mounted) return;
        setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [accessToken, user?.id]);

  const splitFullName = (fullName: string | null): { firstName: string; lastName: string } => {
    if (!fullName) {
      return { firstName: '', lastName: '' };
    }

    const parts = fullName.trim().split(/\s+/);
    if (parts.length === 0) {
      return { firstName: '', lastName: '' };
    }

    const firstName = parts.shift() ?? '';
    const lastName = parts.join(' ');
    return { firstName, lastName };
  };

  const initialFormValues = useMemo<userService.UserProfilePayload>(() => {
    const { firstName, lastName } = splitFullName(profile?.fullName ?? null);
    const email = user?.email ?? '';
    const usernameFallback = email ? email.split('@')[0] : '';

    return {
      email,
      firstName: firstName || usernameFallback,
      lastName,
      phone: profile?.phoneNumber ?? '',
      address: profile?.address ?? '',
      bio: profile?.bio ?? '',
      avatarUrl: profile?.avatarUrl ?? null,
      country: '',
      state: '',
      city: '',
      postalCode: '',
    };
  }, [profile, user?.email]);

  const handleSubmit = async (payload: userService.UserProfilePayload) => {
    if (!user?.id) {
      throw new Error('Missing user id');
    }

    const token = accessToken ?? undefined;
    const isCreate = !profile;
    const action = profile ? userService.updateProfile : userService.createProfile;

    try {
      const result = await action(token, user.id, payload);
      setProfile(result);
      setError(null);
      setSuccessMessage(isCreate ? 'Hồ sơ của bạn đã được tạo.' : 'Thông tin hồ sơ đã được cập nhật.');
    } catch (err) {
      setSuccessMessage(null);
      throw err;
    }
  };

  const handleCloseSuccess = (_event?: SyntheticEvent | Event, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }
    setSuccessMessage(null);
  };

  return (
    <>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Grid container spacing={3} alignItems="flex-start">
          <Grid item xs={12} md={4}>
            <ProfileCard user={user} profile={profile} loading={loading} />
          </Grid>

          <Grid item xs={12} md={8}>
            <Stack spacing={2}>
              {error ? <Alert severity="error">{error}</Alert> : null}

              <ProfileEditForm
                mode={profile ? 'edit' : 'create'}
                initial={initialFormValues}
                onSubmit={handleSubmit}
                loading={loading}
              />
            </Stack>
          </Grid>
        </Grid>
      </Container>

      {successMessage ? (
        <Snackbar
          open
          autoHideDuration={4000}
          onClose={handleCloseSuccess}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        >
          <Alert onClose={handleCloseSuccess} severity="success" variant="filled" sx={{ width: '100%' }}>
            {successMessage}
          </Alert>
        </Snackbar>
      ) : null}
    </>
  );
};

export default ProfilePage;
