# GitHub Setup Commands for ESG Portfolio Analytics

## Step 1: Replace YOUR_USERNAME with your actual GitHub username
git remote set-url origin https://github.com/YOUR_USERNAME/esg-portfolio-analytics.git

## Step 2: Push to GitHub
git push -u origin main

## Step 3: Verify everything is uploaded
git status
git log --oneline

## Alternative: If you want to start fresh (only if needed)
# Remove current remote
git remote remove origin

# Add your new remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/esg-portfolio-analytics.git

# Push to GitHub
git push -u origin main
