import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Grid,
  Paper,
  Skeleton,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import type { UserProfilePayload } from '../services/userService';

interface Props {
  initial: Partial<UserProfilePayload>;
  onSubmit: (payload: UserProfilePayload) => Promise<void> | void;
  mode?: 'create' | 'edit';
  loading?: boolean;
}

const defaultForm: UserProfilePayload = {
  email: '',
  firstName: '',
  lastName: '',
  address: '',
  country: '',
  state: '',
  city: '',
  postalCode: '',
  phone: '',
  bio: '',
  avatarUrl: null,
};

const ProfileEditForm: React.FC<Props> = ({ initial, onSubmit, mode = 'edit', loading = false }) => {
  const [form, setForm] = useState<UserProfilePayload>({ ...defaultForm, ...initial });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setForm({ ...defaultForm, ...initial });
  }, [initial]);

  const handleChange = (field: keyof UserProfilePayload) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => {
    const { value } = event.target;
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const isDisabled = useMemo(() => {
    return (
      loading
      || saving
      || !form.firstName?.trim()
      || !form.lastName?.trim()
      || !form.email?.trim()
      || !form.phone?.trim()
    );
  }, [form.email, form.firstName, form.lastName, form.phone, loading, saving]);

  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isDisabled) return;

    setError(null);
    setSaving(true);
    try {
      await onSubmit(form);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Lỗi khi lưu thông tin');
    } finally {
      setSaving(false);
    }
  };

  const title = mode === 'create' ? 'Create Profile' : 'Edit Profile';
  const description =
    mode === 'create'
      ? 'Nhập thông tin cá nhân của bạn để khởi tạo hồ sơ.'
      : 'Cập nhật thông tin cá nhân của bạn để giữ tài khoản luôn mới nhất.';
  const submitLabel = (() => {
    if (saving) {
      return mode === 'create' ? 'Đang tạo...' : 'Đang lưu...';
    }
    return mode === 'create' ? 'Create Profile' : 'Update Profile';
  })();

  if (loading) {
    return (
      <Paper elevation={0} sx={{ p: 4, borderRadius: 4 }}>
        <Stack spacing={3}>
          <Skeleton variant="text" width="35%" height={32} />
          <Skeleton variant="text" width="60%" height={20} />

          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12} md={6}>
              <Skeleton variant="rounded" height={56} sx={{ borderRadius: 2 }} />
            </Grid>
            <Grid item xs={12}>
              <Skeleton variant="rounded" height={120} sx={{ borderRadius: 2 }} />
            </Grid>
          </Grid>

          <Box textAlign="right">
            <Skeleton variant="rounded" width={160} height={44} sx={{ borderRadius: 2, display: 'inline-block' }} />
          </Box>
        </Stack>
      </Paper>
    );
  }

  return (
    <Paper component="form" elevation={0} onSubmit={submit} sx={{ p: 4, borderRadius: 4 }}>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h6" fontWeight={600} gutterBottom>
            {title}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {description}
          </Typography>
        </Box>

        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <TextField
              required
              label="Email Address"
              value={form.email}
              onChange={handleChange('email')}
              fullWidth
              type="email"
              disabled={saving}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              required
              label="Phone"
              value={form.phone ?? ''}
              onChange={handleChange('phone')}
              fullWidth
              placeholder="Số điện thoại"
              disabled={saving}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              required
              label="First Name"
              value={form.firstName ?? ''}
              onChange={handleChange('firstName')}
              fullWidth
              disabled={saving}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              required
              label="Last Name"
              value={form.lastName ?? ''}
              onChange={handleChange('lastName')}
              fullWidth
              disabled={saving}
            />
          </Grid>

          <Grid item xs={12}>
            <TextField
              label="Address"
              value={form.address ?? ''}
              onChange={handleChange('address')}
              fullWidth
              placeholder="Enter Home Address"
              disabled={saving}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              label="Country"
              value={form.country ?? ''}
              onChange={handleChange('country')}
              fullWidth
              disabled={saving}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              label="State"
              value={form.state ?? ''}
              onChange={handleChange('state')}
              fullWidth
              disabled={saving}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              label="City"
              value={form.city ?? ''}
              onChange={handleChange('city')}
              fullWidth
              disabled={saving}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              label="Postal Code"
              value={form.postalCode ?? ''}
              onChange={handleChange('postalCode')}
              fullWidth
              disabled={saving}
            />
          </Grid>

          <Grid item xs={12}>
            <TextField
              label="Bio"
              value={form.bio ?? ''}
              onChange={handleChange('bio')}
              fullWidth
              multiline
              minRows={4}
              disabled={saving}
            />
          </Grid>
        </Grid>

        {error ? <Alert severity="error">{error}</Alert> : null}

        <Box textAlign="right">
          <Button
            type="submit"
            variant="contained"
            size="large"
            disabled={isDisabled}
            endIcon={saving ? <CircularProgress size={20} thickness={5} /> : undefined}
          >
            {submitLabel}
          </Button>
        </Box>
      </Stack>
    </Paper>
  );
};

export default ProfileEditForm;
