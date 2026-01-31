import { bootstrapApplication } from '@angular/platform-browser';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { App } from './app/app';
import { appConfig } from './app/app.config';

registerLocaleData(localeIt);

bootstrapApplication(App, appConfig)
  .catch(err => console.error(err));
