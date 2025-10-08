#!/bin/bash

# Build and run the frontend Docker container

echo "🚀 Building DocuTrace Frontend Docker Image..."

# Build the Docker image
docker build -t docutrace-frontend:latest \
  --build-arg VITE_API_URL=${VITE_API_URL:-http://localhost:5000/api} \
  --build-arg VITE_DEV_MODE=${VITE_DEV_MODE:-false} \
  --build-arg VITE_USE_MOCK_FALLBACK=${VITE_USE_MOCK_FALLBACK:-false} \
  .

if [ $? -eq 0 ]; then
    echo "✅ Docker image built successfully!"
    
    echo "🏃 Running DocuTrace Frontend..."
    
    # Stop existing container if running
    docker stop docutrace-frontend 2>/dev/null || true
    docker rm docutrace-frontend 2>/dev/null || true
    
    # Run the container
    docker run -d \
      --name docutrace-frontend \
      -p 3000:80 \
      --restart unless-stopped \
      docutrace-frontend:latest
    
    if [ $? -eq 0 ]; then
        echo "✅ Frontend is running at http://localhost:3000"
        echo "📊 View logs: docker logs -f docutrace-frontend"
        echo "🛑 Stop container: docker stop docutrace-frontend"
    else
        echo "❌ Failed to run the container"
        exit 1
    fi
else
    echo "❌ Failed to build Docker image"
    exit 1
fi